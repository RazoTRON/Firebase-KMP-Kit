plugins {
    `kotlin-dsl` // Is needed to turn our build logic written in Kotlin into Gradle Plugin
}

repositories {
    maven("https://packages.jetbrains.team/maven/p/kt/dev")
    gradlePluginPortal() // To use 'maven-publish' and 'signing' plugins in our own plugin
}

dependencies {
    implementation(libs.vanniktech.gradle)
}