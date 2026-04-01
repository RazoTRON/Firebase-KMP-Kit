package task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import java.io.File


fun Project.generateDefFiles(fileName: String, interopFileName: String) {
    val taskName = "generate${fileName}DefFiles"

    abstract class GenerateFirebaseCoreDefFilesTask : DefaultTask() {
        @get:Input
        abstract val packageName: Property<String>

        @get:Input
        abstract val defFileName: Property<String>

        @get:Input
        abstract val libsFileName: Property<String>

        @get:OutputDirectory
        abstract val interopDir: DirectoryProperty

        @TaskAction
        fun generate() {
            interopDir.get().asFile.mkdirs()

            // Generate FirebaseCore.def
            val defFile = File(interopDir.get().asFile, "${defFileName.get()}.def")
            defFile.writeText(defFileContent(libsFileName.get()))
        }

        private fun defFileContent(fileName: String): String {
            val libsDir = interopDir.dir("libs").get().asFile

            return """
                       language = Objective-C
                       package = ${packageName.get()}
                       headers = ${File(libsDir, fileName).absolutePath}
                   """.trimIndent()
        }
    }

    tasks.register<GenerateFirebaseCoreDefFilesTask>(taskName) {
        packageName.set("com.firebasekit.native")
        defFileName.set(fileName)
        libsFileName.set(interopFileName)
        interopDir.set(project.layout.projectDirectory.dir("src/interop"))
        group = "interop"
    }

    tasks.withType<CInteropProcess>()
        .matching { it.name.startsWith("cinterop$fileName") }
        .configureEach {
            dependsOn(tasks.named(taskName))
        }

    tasks.named("build") {
        dependsOn(tasks.named(taskName))
    }
}