import extension.publishLibrary

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("publication")
}

group = "io.github.razotron.firebase-kit"
version = "0.2.0-rc4"

gradlePlugin {
    plugins {
        create("generateJsResource") {
            id = "io.github.razotron.firebasekit.generate-js-resource"
            implementationClass = "io.github.razotron.firebasekit.gradle.GenerateJsResourcePlugin"
            displayName = "FirebaseKit Generate JS Resource Plugin"
            description = "Generates a JavaScript file and wires it into firebaseResources packaging."
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
