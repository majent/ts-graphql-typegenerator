package typeGenerator.generator.impl.model

internal class TypeScriptType private constructor(val type: String, val nullable: Boolean) {
    companion object {
        fun from(type: String, nullable: Boolean): TypeScriptType {
            return TypeScriptType(type, nullable)
        }
    }
}
