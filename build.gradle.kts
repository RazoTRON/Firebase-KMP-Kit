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