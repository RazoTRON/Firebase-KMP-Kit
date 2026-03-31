import extension.defaultTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import task.generateDefFiles

plugins {
    id("build-config")
}
version = "0.0.8"

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
        }
    )

    sourceSets {
        androidMain.dependencies {
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            implementation(devNpm("firebase", libs.versions.firebase.webNpm.remoteConfigs.get()))
        }
    }
}

generateDefFiles("FirebaseCore")