import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

group = "com.firebasekit"
version = "0.0.1"

kotlin {
    val xcf = XCFramework("FirebaseKitCore")

    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
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
    js {
        browser()
        useEsModules()
    }
    wasmJs { browser() }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            implementation(devNpm("firebase", "10.13.2"))
        }
    }
}

android {
    namespace = project.group.toString()
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

generateFirebaseCoreDefFiles()


fun Project.generateFirebaseCoreDefFiles() {
    val taskName = "generateFirebaseCoreDefFiles"

    abstract class GenerateFirebaseCoreDefFilesTask : DefaultTask() {
        @get:Input
        abstract val packageName: Property<String>

        @get:OutputDirectory
        abstract val interopDir: DirectoryProperty

        @TaskAction
        fun generate() {
            interopDir.get().asFile.mkdirs()

            // Generate FirebaseCore.def
            val firebaseCoreDefFile = File(interopDir.get().asFile, "FirebaseCore.def")
            firebaseCoreDefFile.writeText(defFileContent("FirebaseCore"))
        }

        private fun defFileContent(fileName: String): String {
            val libsDir = interopDir.dir("libs").get().asFile

            return """
                       language = Objective-C
                       package = ${packageName.get()}
                       headers = ${File(libsDir, "$fileName.h").absolutePath}
                   """.trimIndent()
        }
    }

    tasks.register<GenerateFirebaseCoreDefFilesTask>(taskName) {
        packageName.set("com.firebasekit.native")
        interopDir.set(project.layout.projectDirectory.dir("src/interop"))
        group = "interop"
    }

    tasks.withType<CInteropProcess>()
        .matching { it.name.startsWith("cinteropFirebaseCore") }
        .configureEach {
            dependsOn(tasks.named(taskName))
        }

    tasks.named("build") {
        dependsOn(tasks.named(taskName))
    }
}