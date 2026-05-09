import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("build-config")
    id("publication")
}

version = "0.2.0-rc3"

kotlin {
    val xcf = XCFramework("FirebaseKitMessaging")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitMessaging"
                binaryOption("bundleId", "FirebaseKitMessaging")
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
            products = listOf(product("FirebaseMessaging")),
            importedClangModules = listOf("FirebaseMessaging"),
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
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            api(libs.firebase.messaging)
        }

        webMain.dependencies {
            api(devNpm("firebase", libs.versions.firebase.webNpm.get()))
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

buildLibrary(libraryName = "FirebaseKitMessaging")

publishLibrary(
    name = "Firebase KMP Kit",
    description = "A Kotlin Multiplatform library that provides Firebase Services in common code.",
    artifactId = "messaging"
)
