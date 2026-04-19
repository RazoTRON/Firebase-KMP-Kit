import extension.GeneratedJsResourceExtension
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import task.GenerateJsResourceTask

val generatedJsResource = extensions.create<GeneratedJsResourceExtension>("generatedJsResource").apply {
    fileName.convention("generated.js")
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
    val generatedResourcesDir = layout.buildDirectory.dir("generated/resources/js/commonMain")

    val generateJsResource by tasks.register<GenerateJsResourceTask>("generateJsResource") {
        group = "generation"
        description = "Generates a JavaScript resource file for the commonMain source set."
        fileName.set(generatedJsResource.fileName)
        content.set(generatedJsResource.content)
        outputDirectory.set(generatedResourcesDir)
    }

    kotlin.sourceSets.named("commonMain") {
        resources.srcDir(generatedResourcesDir)
    }

    tasks.matching { it.name.endsWith("ProcessResources") }.configureEach {
        dependsOn(generateJsResource)
    }
}
