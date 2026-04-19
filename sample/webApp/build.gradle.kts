import io.github.razotron.firebasekit.gradle.generateJsResources
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    id("io.github.razotron.firebasekit.generate-js-resource")
}

val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.sample.shared)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

generateJsResources(
    apiKey = properties.getProperty("FIREBASE_API_KEY"),
    authDomain = properties.getProperty("FIREBASE_AUTH_DOMAIN"),
    projectId = properties.getProperty("FIREBASE_PROJECT_ID"),
    storageBucket = properties.getProperty("FIREBASE_STORAGE_BUCKET"),
    messagingSenderId = properties.getProperty("FIREBASE_MESSAGING_SENDER_ID"),
    appId = properties.getProperty("FIREBASE_APP_ID"),
    measurementId = properties.getProperty("FIREBASE_MEASUREMENT_ID"),
)
