package typeGenerator

import typeGenerator.generator.impl.GraphQLTypeGenerator
import typeGenerator.generator.impl.TypeScriptTypeGenerator
import typeGenerator.generator.impl.model.Definitions
import typeGenerator.visitor.ClassVisitor
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

class TypeGenerator {

    companion object {

        /**
         * generate TypeScript type and GraphQL schema
         *
         * @param classes
         * @return
         */
        fun generate(classes: Iterable<KClass<*>>, packageName: String? = null): List<Definitions> {
            val generators = listOf(TypeScriptTypeGenerator(packageName), GraphQLTypeGenerator())
            val visitor = ClassVisitor(generators)

            classes.forEach { visitor.visitClass(it) }
            return generators.map { it.definitions }
        }

        /**
         * write out definitions
         *
         * @param definitions
         * @param rootDir
         */
        fun writeOut(definitions: List<Definitions>, rootDir: String = "type-generator") {
            definitions.forEach {
                val definitionTypeDir = "$rootDir/${it.definitionType.dir}"
                createDirectory(definitionTypeDir)
                it.definitions.forEach { definition ->
                    File("$definitionTypeDir/${definition.fileName}").bufferedWriter().use { out ->
                        out.write(definition.definition)
                    }
                }
            }
        }

        private fun createDirectory(root: String) {
            if (!File(root).exists()) {
                Files.createDirectories(Paths.get(root))
            }
        }
    }

}
