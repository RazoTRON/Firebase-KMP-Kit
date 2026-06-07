import extension.buildLibrary
import extension.defaultTargets
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("build-config")
    id("publication")
}

version = providers.gradleProperty("firebaseKitVersion").get()

kotlin {
    val xcf = XCFramework("FirebaseKitCrashlytics")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitCrashlytics"
                binaryOption("bundleId", "FirebaseKitCrashlytics")
                xcf.add(this)
                isStatic = true
            }

            it.compilations["main"].cinterops {
                create("crashlytics") {
                    defFile(project.layout.projectDirectory.file("src/interop/crashlytics.def"))
                }
            }
        }
    )

    swiftPMDependencies {
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = from(libs.versions.firebase.swiftPM.get()),
            products = listOf(product("FirebaseCrashlytics")),
            importedClangModules = listOf("FirebaseCrashlyticsInternal"),
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
        }

        androidMain.dependencies {
            implementation(libs.firebase.crashlytics)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

buildLibrary(libraryName = "FirebaseKitCrashlytics")

publishLibrary(
    name = "Firebase KMP Kit",
    description = "A Kotlin Multiplatform library that provides Firebase Services in common code.",
    artifactId = "crashlytics"
)
