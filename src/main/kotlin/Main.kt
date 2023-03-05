import typeGenerator.TypeGenerator

fun main(args: Array<String>) {
    val allClasses = listOf(
        "model.SampleDto"
    ).map { Class.forName(it).kotlin }.toSet()

    val definitions = TypeGenerator.generate(classes = allClasses)
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