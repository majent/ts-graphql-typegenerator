package typeGenerator.generator.impl.model

enum class DefinitionType(val dir: String) {
    GRAPHQL("graphql/schema/"),
    TYPESCRIPT("graphql/model/")
}

data class Definitions(

    /**
     * GraphQL or TypeScript
     */
    val definitionType: DefinitionType,

    val definitions: List<Definition>,
)

data class Definition(

    val fileName: String,

    val definition: String,
)
