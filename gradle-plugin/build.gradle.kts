import extension.publishLibrary
import java.io.FileInputStream
import java.util.Properties

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("publication")
}

group = "io.github.razotron.firebase-kit"
version = "0.2.0-rc3"

val parentGradleProperties = rootProject.file("../gradle.properties")

if (parentGradleProperties.exists()) {
    val properties = Properties().apply {
        load(FileInputStream(parentGradleProperties))
    }

    listOf(
        "mavenCentralUsername",
        "mavenCentralPassword",
        "signing.keyId",
        "signing.password",
        "signing.secretKeyRingFile",
    ).forEach { key ->
        if (findProperty(key) == null) {
            extensions.extraProperties[key] = properties.getProperty(key)
        }
    }
}

gradlePlugin {
    plugins {
        create("generateJsResource") {
            id = "io.github.razotron.firebasekit.generate-js-resource"
            implementationClass = "io.github.razotron.firebasekit.gradle.GenerateJsResourcePlugin"
            displayName = "FirebaseKit Generate JS Resource Plugin"
            description = "Generates a JavaScript file and wires it into Kotlin Multiplatform resources."
        }
    }
}

publishLibrary(
    name = "Firebase KMP Kit Generate JS Resource Gradle Plugin",
    description = "A Gradle plugin that generates JavaScript resource files and adds them to Kotlin Multiplatform resources.",
    artifactId = "gradle-plugin"
)

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.android.gradle)

    // h4x so we can access version catalog from convention script
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
