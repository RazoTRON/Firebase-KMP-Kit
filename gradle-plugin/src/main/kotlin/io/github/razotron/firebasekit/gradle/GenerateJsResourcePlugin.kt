package io.github.razotron.firebasekit.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class GenerateJsResourcePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<GeneratedJsResourceExtension>("generatedJsResource")

        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            val outputDir = project.layout.buildDirectory.dir("generated/resources/webMain")

            val generateTask = tasks.register<GenerateJsResourceTask>("generateJsResource") {
                outputDirectory.set(outputDir)
                fileName.set(extension.fileName)
                content.set(extension.content)
            }

            kotlin.sourceSets.named("commonMain") {
                resources.srcDir(outputDir)
            }

            project.tasks.matching { it.name.endsWith("ProcessResources") }.configureEach {
                dependsOn(generateTask)
            }
        }
    }
}
