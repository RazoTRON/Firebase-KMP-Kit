import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import task.generateDefFiles

plugins {
    id("build-config")
    id("publication")
    alias(libs.plugins.kotlinx.serialization)
}

version = "0.1.4"

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

            it.compilations["main"].cinterops {
                create("RemoteConfig") {
                    defFile(project.layout.projectDirectory.file("src/interop/RemoteConfig.def"))
                }
            }
        },
        jsConfig = {
            compilations["main"].packageJson {
                customField("dependencies", mapOf("firebase" to libs.versions.firebase.webNpm.remoteConfigs.get()))
            }
        }
    )

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
            api(devNpm("firebase", libs.versions.firebase.webNpm.remoteConfigs.get()))
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

generateDefFiles(
    fileName = "RemoteConfig",
    interopFileName = "FIRRemoteConfig.h"
)

buildLibrary()

publishLibrary(artifactId = "remote-config")