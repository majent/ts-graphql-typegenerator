package typeGenerator.generator.impl

import com.google.common.base.CaseFormat
import typeGenerator.generator.Generator
import typeGenerator.generator.Generator.Companion.KotlinAnyOrNull
import typeGenerator.generator.impl.model.Definition
import typeGenerator.generator.impl.model.DefinitionType
import typeGenerator.generator.impl.model.TypeScriptType
import typeGenerator.visitor.ClassVisitor
import java.math.BigDecimal
import javax.annotation.Nonnull
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

internal class TypeScriptTypeGenerator(val packageName: String?) : Generator {

    override val definitionType = DefinitionType.TYPESCRIPT
    override val generatedDefinitions = mutableListOf<Definition>()
    private val mappings: Map<KClass<*>, String> = mapOf()

    override fun generateDefinition(klass: KClass<*>, classVisitor: ClassVisitor) {
        if (klass.java.isEnum) {
            generatedDefinitions.add(Definition("${formatClassName(klass)}.ts", generateEnum(klass)))
        } else {
            generatedDefinitions.add(Definition("${formatClassName(klass)}.ts", generateType(klass, classVisitor)))
        }
    }

    override fun formatClassName(klass: KClass<*>): String {
        return klass.simpleName ?: "MyClass"
    }

    private fun generateEnum(klass: KClass<*>): String {
        return "export enum ${formatClassName(klass)} {\n${
            klass.java.enumConstants
                .map { constant: Any ->
                    constant.toString()
                }.joinToString("") {
                    "  ${CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, it)} = '$it',\n"
                }
        }}\n"
    }

    private fun generateType(klass: KClass<*>, classVisitor: ClassVisitor): String {
        val isKotlin = klass.java.getAnnotation(Metadata::class.java) !== null
        val templateParameters = if (klass.typeParameters.isNotEmpty()) {
            "<" + klass.typeParameters
                .map { typeParameter ->
                    val bounds = typeParameter.upperBounds
                        .filter { it.classifier != Any::class }
                    typeParameter.name + if (bounds.isNotEmpty()) {
                        " extends " + bounds
                            .map { bound ->
                                formatKType(typeParameter.name, bound, isKotlin, primitive = false, nonNull = true, classVisitor).type
                            }
                            .joinToString(" & ")
                    } else {
                        ""
                    }
                }
                .joinToString(", ") + ">"
        } else {
            ""
        }

        val imports = klass.declaredMemberProperties.filter {
            if (packageName !== null) {
                it.returnType.javaType.typeName.startsWith(packageName)
            } else {
                false
            }
        }.mapNotNull {
            val classifier = it.returnType.classifier
            if (classifier is KClass<*>) {
                "import {${formatClassName(classifier)}} from './${formatClassName(classifier)}';"
            } else {
                null
            }
        }.joinToString("\n").let {
            if (it.isNotEmpty()) "$it\n\n" else ""
        }

        return imports +
            "export type ${formatClassName(klass)}$templateParameters = {\n" +
            klass.declaredMemberProperties
                .filter {
                    filterProperties(it, klass)
                }.joinToString("") { property ->
                    val propertyName = property.name
                    val propertyType = property.returnType
                    val primitive = property.javaField?.type?.isPrimitive ?: false

                    val type = formatKType(propertyName, propertyType, isKotlin, primitive, property.hasAnnotation<Nonnull>(), classVisitor)
                    val markNullable = if (type.nullable) "?" else ""
                    "  $propertyName$markNullable: ${type.type};\n"
                } +
            "};\n"
    }

    private fun formatKType(
        propertyName: String,
        kType: KType,
        isKotlin: Boolean,
        primitive: Boolean,
        nonNull: Boolean,
        classVisitor: ClassVisitor,
    ): TypeScriptType {
        val classifier = kType.classifier
        if (classifier is KClass<*> && mappings[classifier] != null) {
            return TypeScriptType.from(mappings[classifier]!!, kType.isMarkedNullable)
        }

        val classifierTsType = when (classifier) {
            Boolean::class -> "boolean"
            String::class, Char::class, BigDecimal::class -> "string"
            Int::class,
            Long::class,
            Short::class,
            Byte::class,
            -> "number"
            Float::class, Double::class -> "number"
            Any::class -> "any"
            else -> {
                formatObjectKType(classifier, kType, propertyName, isKotlin, classVisitor)
            }

        }

        return TypeScriptType.from(classifierTsType, isNullable(nonNull, primitive, isKotlin, kType.isMarkedNullable))
    }

    private fun formatObjectKType(
        classifier: KClassifier?,
        kType: KType,
        propertyName: String,
        isKotlin: Boolean,
        classVisitor: ClassVisitor,
    ): String {
        return if (classifier is KClass<*>) {
            if (classifier.isSubclassOf(Iterable::class)
                || classifier.javaObjectType.isArray
            ) {
                val itemType = convertKTypeFromIterableGenericsType(kType)
                "${formatKType(propertyName, itemType, isKotlin, primitive = false, nonNull = true, classVisitor).type}[]"
            } else if (classifier.isSubclassOf(Map::class)) {
                val rawKeyType = kType.arguments[0].type ?: KotlinAnyOrNull
                val keyType = formatKType(propertyName, rawKeyType, isKotlin, primitive = false, nonNull = true, classVisitor)
                val valueType = formatKType(propertyName, kType.arguments[1].type ?: KotlinAnyOrNull, isKotlin, primitive = false, nonNull = true, classVisitor)
                if ((rawKeyType.classifier as? KClass<*>)?.java?.isEnum == true)
                    "{[key in ${keyType.type}]: ${valueType.type}}"
                else
                    "Record<${keyType.type}, ${valueType.type}>"
            } else {
                formatClassType(classifier, classVisitor) + if (kType.arguments.isNotEmpty()) {
                    "<" + kType.arguments
                        .map { arg -> formatKType(propertyName, arg.type ?: KotlinAnyOrNull, isKotlin, primitive = false, nonNull = true, classVisitor).type }
                        .joinToString(", ") + ">"
                } else ""
            }
        } else if (classifier is KTypeParameter) {
            classifier.name
        } else {
            "UNKNOWN"
        }
    }
}
