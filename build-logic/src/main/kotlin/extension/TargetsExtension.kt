package extension

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun KotlinMultiplatformExtension.defaultTargets(iOSConfig: ((KotlinNativeTarget) -> Unit)? = null) {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        iOSConfig?.invoke(it)
    }

    js {
        browser()
        useEsModules()
    }

    wasmJs {
        browser()
    }
}