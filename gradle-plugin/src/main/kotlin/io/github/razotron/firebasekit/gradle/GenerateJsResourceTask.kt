package io.github.razotron.firebasekit.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

private const val WEB_NPM_VERSION = "10.13.2"

abstract class GenerateJsResourceTask : DefaultTask() {
    @get:Input
    abstract val fileName: Property<String>

    @get:Input
    abstract val content: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile.apply { mkdirs() }
        File(outputDir, fileName.get()).writeText(content.get())
    }
}

fun Project.generateJsResources(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
    measurementId: String,
) {
    extensions.getByType<GeneratedJsResourceExtension>().apply {
        fileName.set("firebase-messaging-sw.js")
        content.set(
            """
        importScripts('https://www.gstatic.com/firebasejs/$WEB_NPM_VERSION/firebase-app-compat.js');
        importScripts('https://www.gstatic.com/firebasejs/$WEB_NPM_VERSION/firebase-messaging-compat.js');

        firebase.initializeApp({
          apiKey: "$apiKey",
          authDomain: "$authDomain",
          projectId: "$projectId",
          storageBucket: "$storageBucket",
          messagingSenderId: "$messagingSenderId",
          appId: "$appId",
          measurementId: "$measurementId"
        });

        firebase.messaging();
        """.trimIndent()
        )
    }
}