package typeGenerator.generator.impl.model

internal class GraphQLType private constructor(val type: String, val nullable: Boolean) {
    companion object {
        fun from(type: String, nullable: Boolean): GraphQLType {
            return GraphQLType(if (nullable) type else "$type!", nullable)
        }
    }
}
