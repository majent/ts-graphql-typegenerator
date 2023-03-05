package typeGenerator.generator.impl

import com.google.common.base.CaseFormat
import typeGenerator.generator.Generator
import typeGenerator.generator.Generator.Companion.KotlinAnyOrNull
import typeGenerator.generator.impl.model.Definition
import typeGenerator.generator.impl.model.DefinitionType
import typeGenerator.generator.impl.model.GraphQLType
import typeGenerator.visitor.ClassVisitor
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField

internal class GraphQLTypeGenerator : Generator {

    override val definitionType = DefinitionType.GRAPHQL
    override val generatedDefinitions = mutableListOf<Definition>()
    private val mappings: Map<KClass<*>, String> = mapOf()

    companion object {
        val REGEX_DTO = "Dto$".toRegex()
    }

    override fun generateDefinition(klass: KClass<*>, classVisitor: ClassVisitor) {
        if (klass.java.isEnum) {
            generatedDefinitions.add(Definition("${formatClassName(klass)}.graphqls", generateEnum(klass)))
        } else {
            generatedDefinitions.add(Definition("${formatClassName(klass)}.graphqls", generateType(klass, classVisitor)))
        }
    }

    override fun formatClassName(klass: KClass<*>): String {
        return klass.simpleName?.replace(REGEX_DTO, "") ?: "MyClass"
    }

    private fun generateEnum(klass: KClass<*>): String {
        return "enum ${formatClassName(klass)} {\n${
            klass.java.enumConstants
                .map { constant: Any ->
                    constant.toString()
                }.joinToString("") {
                    "  $it\n"
                }
        }}\n"
    }

    private fun generateType(klass: KClass<*>, classVisitor: ClassVisitor): String {
        val isKotlin = klass.java.getAnnotation(kotlin.Metadata::class.java) !== null
        val klassName = formatClassName(klass)
        return "type $klassName {\n" +
            klass.declaredMemberProperties
                .filter {
                    filterProperties(it, klass)
                }.joinToString("") { property ->
                    val propertyName = property.name
                    val propertyType = property.returnType
                    val primitive = property.javaField?.type?.isPrimitive ?: false

                    val formattedPropertyType =
                        formatKType(klassName, propertyName, propertyType, isKotlin, primitive, nonNull =false, classVisitor).type
                    "  $propertyName: $formattedPropertyType\n"
                } +
            "}\n"
    }

    private fun generateMapEntryType(propertyName: String, keyType: GraphQLType, valueType: GraphQLType): String {
        return """
            type $propertyName {
              key : ${keyType.type}
              value : ${valueType.type}
            }
            
        """.trimIndent()
    }

    private fun formatKType(
        klassName: String,
        propertyName: String,
        kType: KType,
        isKotlin: Boolean,
        primitive: Boolean,
        nonNull: Boolean,
        classVisitor: ClassVisitor,
    ): GraphQLType {
        val classifier = kType.classifier
        if (classifier is KClass<*> && mappings[classifier] != null) {
            return GraphQLType.from(mappings[classifier]!!, kType.isMarkedNullable)
        }

        val classifierTsType = when (classifier) {
            Boolean::class -> "Boolean"
            String::class, Char::class, BigDecimal::class -> "String"
            Int::class,
            Long::class,
            Short::class,
            Byte::class,
            -> "Int"
            Float::class, Double::class -> "Float"
            Any::class -> "TODO"
            else -> {
                formatObjectKType(classifier, kType, klassName, propertyName, isKotlin, classVisitor)
            }

        }

        return GraphQLType.from(classifierTsType, isNullable(nonNull, primitive, isKotlin, kType.isMarkedNullable))
    }

    private fun formatObjectKType(
        classifier: KClassifier?,
        kType: KType,
        klassName: String,
        propertyName: String,
        isKotlin: Boolean,
        classVisitor: ClassVisitor,
    ): String {
        return if (classifier is KClass<*>) {
            if (classifier.isSubclassOf(Iterable::class)
                || classifier.javaObjectType.isArray
            ) {
                val itemType = convertKTypeFromIterableGenericsType(kType)
                "[${formatKType(klassName, propertyName, itemType, isKotlin, primitive = false, nonNull = true, classVisitor).type}]"
            } else if (classifier.isSubclassOf(Map::class)) {
                val rawKeyType = kType.arguments[0].type ?: KotlinAnyOrNull
                val keyType = formatKType(klassName, propertyName, rawKeyType, isKotlin, primitive = false, nonNull = true, classVisitor)
                val valueType = formatKType(klassName, propertyName, kType.arguments[1].type ?: KotlinAnyOrNull, isKotlin, primitive = false, nonNull = true, classVisitor)
                val mapEntryTypeName = "${CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propertyName)}Entry"
                generatedDefinitions.add(Definition("${klassName}.graphqls", generateMapEntryType(mapEntryTypeName, keyType, valueType)))
                "[$mapEntryTypeName!]"
            } else {
                formatClassType(classifier, classVisitor)
            }
        } else if (classifier is KTypeParameter) {
            classifier.name
        } else {
            "UNKNOWN"
        }
    }
}
