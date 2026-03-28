import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

kotlin {
    androidTarget() //We need the deprecated target to have working previews
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "shared"
                    isStatic = true
                }
            }
        }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.remoteConfig)

            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    // Generate BuildConfig for the sample module (available from commonMain)
    buildConfig {
         packageName("com.firebasekit")
         useKotlinOutput { internalVisibility = false }
         buildConfigField("FIREBASE_API_KEY", properties.getProperty("FIREBASE_API_KEY"))
         buildConfigField("FIREBASE_PROJECT_ID", properties.getProperty("FIREBASE_PROJECT_ID"))
         buildConfigField("FIREBASE_APP_ID", properties.getProperty("FIREBASE_APP_ID"))
         buildConfigField("FIREBASE_MESSAGING_SENDER_ID", properties.getProperty("FIREBASE_MESSAGING_SENDER_ID"))
         buildConfigField("FIREBASE_STORAGE_BUCKET", properties.getProperty("FIREBASE_STORAGE_BUCKET"))
         buildConfigField("FIREBASE_AUTH_DOMAIN", properties.getProperty("FIREBASE_AUTH_DOMAIN"))
         buildConfigField("FIREBASE_MEASUREMENT_ID", properties.getProperty("FIREBASE_MEASUREMENT_ID"))
    }
}

compose {
    resources {
        packageOfResClass = "com.firebasekit.sample.resources"
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "com.firebasekit.sample"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
