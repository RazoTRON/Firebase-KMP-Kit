plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.google.services).apply(false)
}

subprojects {
    if (project.path.startsWith(":sample")) {
        pluginManager.withPlugin("maven-publish") {
            tasks.withType<AbstractPublishToMaven>().configureEach {
                enabled = false
            }
        }
    }
}

// Disable Google Services tasks when google-services.json is absent (e.g. CI / JitPack)
gradle.taskGraph.whenReady {
    allTasks
        .filter { task ->
            task.project.path.startsWith(":sample") &&
                task.name.contains("GoogleServices", ignoreCase = true) &&
                !task.project.file("google-services.json").exists()
        }
        .forEach { it.enabled = false }
}