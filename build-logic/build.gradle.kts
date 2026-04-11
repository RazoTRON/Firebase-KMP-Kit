plugins {
    `kotlin-dsl` // Is needed to turn our build logic written in Kotlin into Gradle Plugin
}

repositories {
    maven("https://packages.jetbrains.team/maven/p/kt/dev")
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.android.gradle)

    // h4x so we can access version catalog from convention script
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
