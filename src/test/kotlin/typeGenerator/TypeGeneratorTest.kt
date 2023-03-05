package typeGenerator

import org.junit.jupiter.api.Test

internal class TypeGeneratorTest {

    @Test
    fun test() {
        val allClasses = listOf(
            "typeGenerator.model.SampleModel",
            "typeGenerator.model.SampleModelKt"
        ).map { Class.forName(it).kotlin }.toSet()

        val definitions = TypeGenerator.generate(classes = allClasses, packageName = " typeGenerator")
        TypeGenerator.writeOut(definitions)

        definitions.forEach {
            it.definitions.forEach {
                println("******************************************************")
                println("* ${it.fileName}")
                println("******************************************************\n")
                println(it.definition)
                println("\n")
            }
        }
    }
}