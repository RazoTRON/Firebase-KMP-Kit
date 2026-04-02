import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import task.generateDefFiles

plugins {
    id("build-config")
    id("publication")
}
version = "0.1.5"

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

            it.compilations["main"].cinterops {
                create("FirebaseCore") {
                    defFile(project.layout.projectDirectory.file("src/interop/FirebaseCore.def"))
                }
            }
        },
        jsConfig = {
            compilations["main"].packageJson {
                customField("dependencies", mapOf("firebase" to libs.versions.firebase.webNpm.get()))
            }
        }
    )

    sourceSets {
        androidMain.dependencies {
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            api(devNpm("firebase", libs.versions.firebase.webNpm.get()))
        }
    }
}

generateDefFiles(
    fileName = "FirebaseCore",
    interopFileName = "FirebaseCore.h"
)

publishLibrary(artifactId = "core")