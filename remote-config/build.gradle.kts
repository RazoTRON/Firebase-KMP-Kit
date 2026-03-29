import extension.buildLibrary
import extension.publish.githubPublishConfiguration
import extension.publish.publishAndroidLibraryToMavenLocal
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.Project

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    id("publication")
}

val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

group = "com.firebasekit"
version = "0.0.3"

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
            implementation(devNpm("firebase", "10.13.2"))
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

githubPublishConfiguration(
    httpGitUrl = "https://github.com/RazoTRON/Firebase-KMP-Kit",
    contactEmail = "vmihalatiuk@gmail.com",
    owner = "RazoTRON",
    repo = "Firebase-KMP-Kit",
    groupId = project.group.toString(),
    version = project.version.toString(),
    projectName = project.name,
    projectDescription = project.description.toString(),
    developerId = "RazoTRON",
    developerName = "Vladislav Mihalatiuk"
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

            // Generate RemoteConfig.def
            val remoteConfigDefFile = File(interopDir.get().asFile, "RemoteConfig.def")
            remoteConfigDefFile.writeText(defFileContent("FirebaseRemoteConfig"))
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