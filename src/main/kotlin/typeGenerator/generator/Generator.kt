package typeGenerator.generator

import com.fasterxml.jackson.annotation.JsonIgnore
import typeGenerator.generator.impl.model.Definition
import typeGenerator.generator.impl.model.DefinitionType
import typeGenerator.generator.impl.model.Definitions
import typeGenerator.visitor.ClassVisitor
import java.beans.Introspector
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType

interface Generator {

    companion object {
        val KotlinAnyOrNull = Any::class.createType(nullable = true)
    }

    val definitionType: DefinitionType
    val generatedDefinitions: MutableList<Definition>

    val definitions: Definitions
        get() = generatedDefinitions.groupBy {
            it.fileName
        }.map {
            Definition(it.key, it.value.map { it.definition }.joinToString("\n\n"))
        }.let {
            Definitions(definitionType, it)
        }

    fun generateDefinition(klass: KClass<*>, classVisitor: ClassVisitor)

    fun formatClassName(klass: KClass<*>): String

    fun isJavaBeanProperty(kProperty: KProperty<*>, klass: KClass<*>): Boolean {
        val beanInfo = Introspector.getBeanInfo(klass.java)
        return beanInfo.propertyDescriptors
            .any { bean -> bean.name == kProperty.name }
    }

    fun isFunctionType(javaType: Type): Boolean {
        return javaType is KCallable<*>
            || javaType.typeName.startsWith("kotlin.jvm.functions.")
            || (javaType is ParameterizedType && isFunctionType(javaType.rawType))
    }

    fun filterProperties(it: KProperty1<out Any, *>, klass: KClass<*>) =
        !isFunctionType(it.returnType.javaType) &&
            (it.visibility == KVisibility.PUBLIC || isJavaBeanProperty(it, klass)) &&
            !it.hasAnnotation<JsonIgnore>()

    fun convertKTypeFromIterableGenericsType(kType: KType): KType {
        val itemType = when (kType.classifier) {
            IntArray::class -> Int::class.createType(nullable = false)
            ShortArray::class -> Short::class.createType(nullable = false)
            ByteArray::class -> Byte::class.createType(nullable = false)
            CharArray::class -> Char::class.createType(nullable = false)
            LongArray::class -> Long::class.createType(nullable = false)
            FloatArray::class -> Float::class.createType(nullable = false)
            DoubleArray::class -> Double::class.createType(nullable = false)
            else -> kType.arguments.single().type ?: KotlinAnyOrNull
        }
        return itemType
    }

    fun formatClassType(type: KClass<*>, classVisitor: ClassVisitor): String {
        classVisitor.visitClass(type)
        return formatClassName(type)
    }

    fun clear() {
        generatedDefinitions.clear()
    }

    fun isNullable(nonNull: Boolean, primitive: Boolean, isKotlin: Boolean, isMarkedNullable: Boolean): Boolean {
        return if (nonNull) {
            false
        } else if (primitive) {
            false
        } else if (isKotlin) {
            isMarkedNullable
        } else {
            true
        }
    }
}
