import extension.buildLibrary
import extension.publish.publishAndroidLibraryToMavenLocal
import extension.publishLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.FileInputStream
import java.util.Properties

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
    }
    js {
        browser()
        useEsModules()
    }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
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