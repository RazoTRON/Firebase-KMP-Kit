package io.github.razotron.firebasekit.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class GenerateJsResourcePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<GeneratedJsResourceExtension>("generatedJsResource").apply {
            fileName.convention("generated.js")
            sourceSetName.convention("commonMain")
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
            val generatedResourcesDir = layout.buildDirectory.dir("generated/resources/js/${extension.sourceSetName.get()}")

            val generateJsResource = tasks.register<GenerateJsResourceTask>("generateJsResource") {
                group = "generation"
                description = "Generates a JavaScript resource file for a Kotlin Multiplatform source set."
                fileName.set(extension.fileName)
                content.set(extension.content)
                outputDirectory.set(generatedResourcesDir)
            }

            kotlin.sourceSets.named(extension.sourceSetName.get()) {
                resources.srcDir(generatedResourcesDir)
            }

            tasks.matching { it.name.endsWith("ProcessResources") }.configureEach {
                dependsOn(generateJsResource)
            }
        }
    }
}
