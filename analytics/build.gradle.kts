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
    val xcf = XCFramework("FirebaseKitAnalytics")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitAnalytics"
                binaryOption("bundleId", "FirebaseKitAnalytics")
                xcf.add(this)
                isStatic = true
            }
        }
    )

    swiftPMDependencies {
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = from("12.12.0"),
            products = listOf(product("FirebaseAnalytics")),
            importedClangModules = listOf("FirebaseAnalytics"),
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
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            api(libs.firebase.analytics)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

buildLibrary(libraryName = "FirebaseKitAnalytics")

publishLibrary(
    name = "Firebase KMP Kit",
    description = "A Kotlin Multiplatform library that provides Firebase Services in common code.",
    artifactId = "analytics"
)
