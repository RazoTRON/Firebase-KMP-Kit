import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("build-config")
    id("publication")
    alias(libs.plugins.kotlinx.serialization)
}

version = "0.2.0-rc2"

kotlin {
    val xcf = XCFramework("FirebaseKitRemoteConfig")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitRemoteConfig"
                binaryOption("bundleId", "FirebaseKitRemoteConfig")
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
            products = listOf(product("FirebaseRemoteConfig")),
            importedClangModules = listOf("FirebaseRemoteConfigInternal"),
        )
    }

    sourceSets.configureEach {
        languageSettings {
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.firebase.remoteConfigs)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        webMain.dependencies {
            api(devNpm("firebase", libs.versions.firebase.webNpm.get()))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.client.mock)
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

buildLibrary()

publishLibrary(artifactId = "remote-config")