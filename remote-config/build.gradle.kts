import extension.buildLibrary
import extension.publish.publishAndroidLibraryToMavenLocal
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.Project

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    id("publication")
}

val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

group = "com.firebasekit.remoteconfig"
version = "0.0.1"

kotlin {
    val xcf = XCFramework("FirebaseKitRemoteConfig")

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
    }
    js {
        browser()
        useEsModules()
    }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.firebase.remoteConfigs)
        }

        webMain.dependencies {
            implementation(devNpm("firebase", "10.13.2"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    // https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"]
            .compilerOptions.options.freeCompilerArgs
            .add("-Xexport-kdoc")
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

generateFirebaseConfigDefFiles()

buildLibrary()

publishLibrary(
    gitlabAccesToken = properties.getProperty("gitubAccessToken"),
    projectId = "000000000",
    branch = "dev",
    httpGitUrl = "https://github.com/RazoTRON/Firebase-KMP-Kit",
    contactEmail = "vmihalatiuk@gmail.com",
    groupId = project.group.toString(),
    version = project.version.toString(),
    projectName = project.name,
    projectDescription = project.description.toString()
)

publishAndroidLibraryToMavenLocal(
    groupId = project.group.toString(),
    version = project.version.toString(),
    projectName = project.name,
    projectDescription = project.name
)

fun Project.generateFirebaseConfigDefFiles() {
    val taskName = "generateFirebaseConfigDefFiles"

    abstract class GenerateFirebaseConfigDefFilesTask : DefaultTask() {
        @get:Input
        abstract val packageName: Property<String>

        @get:OutputDirectory
        abstract val interopDir: DirectoryProperty

        @TaskAction
        fun generate() {
            interopDir.get().asFile.mkdirs()
            val firebaseConfigHeaders = "FirebaseRemoteConfig.h"

            // Helper function to generate header paths
            fun headerPath(): String {
                return interopDir.dir("libs/$firebaseConfigHeaders")
                    .get().asFile.absolutePath
            }

            val defFile = File(interopDir.get().asFile, "RemoteConfig.def")
            val content = """
                language = Objective-C
                package = ${packageName.get()}
                headers = ${headerPath()}
            """.trimIndent()

            defFile.writeText(content)
        }
    }

    tasks.register<GenerateFirebaseConfigDefFilesTask>(taskName) {
        packageName.set("com.firebasekit.native")
        interopDir.set(project.layout.projectDirectory.dir("src/interop"))
        group = "interop"
    }

    tasks.withType<CInteropProcess>()
        .matching { it.name.startsWith("cinteropRemoteConfig") }
        .configureEach {
            dependsOn(tasks.named(taskName))
        }

    tasks.named("build") {
        dependsOn(tasks.named(taskName))
    }
}