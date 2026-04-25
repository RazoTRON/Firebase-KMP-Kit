import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinx.serialization)
    id("build-config")
    id("publication")
}

version = "0.2.0-rc3"

kotlin {
    val xcf = XCFramework("FirebaseKitCore")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitCore"
                binaryOption("bundleId", "FirebaseKitCore")
                xcf.add(this)
                isStatic = true
            }
        },
        jsConfig = {
            compilations["main"].packageJson {
                customField("dependencies", mapOf("firebase" to libs.versions.firebase.webNpm.get()))
            }
        }
    )

    swiftPMDependencies {
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = from("12.12.0"),
            products = listOf(product("FirebaseCore")),
            importedClangModules = listOf("FirebaseCore"),
        )
    }

    sourceSets.configureEach {
        languageSettings {
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        androidMain.dependencies {
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            api(devNpm("firebase", libs.versions.firebase.webNpm.get()))
        }
    }
}

buildLibrary(libraryName = "FirebaseKitCore")

publishLibrary(
    name = "Firebase KMP Kit",
    description = "A Kotlin Multiplatform library that provides Firebase Services in common code.",
    artifactId = "core"
)
