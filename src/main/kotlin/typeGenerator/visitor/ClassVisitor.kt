package typeGenerator.visitor

import typeGenerator.generator.Generator
import kotlin.reflect.KClass

class ClassVisitor(private val typeGenerators: List<Generator>) {

    private val visitedClasses: MutableSet<KClass<*>> = java.util.HashSet()

    fun visitClass(klass: KClass<*>) {
        if (klass !in visitedClasses) {
            visitedClasses.add(klass)

            typeGenerators.forEach { generator ->
                generator.generateDefinition(klass, this)
            }
        }
    }

    fun clear() {
        visitedClasses.clear()
    }
}
