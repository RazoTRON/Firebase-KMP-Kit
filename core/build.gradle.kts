import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("build-config")
    id("publication")
}

version = "0.2.0-rc2"

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
        androidMain.dependencies {
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            api(devNpm("firebase", libs.versions.firebase.webNpm.get()))
        }
    }
}

buildLibrary()

publishLibrary(artifactId = "core")