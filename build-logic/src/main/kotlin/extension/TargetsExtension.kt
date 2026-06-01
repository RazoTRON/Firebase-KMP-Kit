package extension

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

fun KotlinMultiplatformExtension.defaultTargets(
    iOSConfig: ((KotlinNativeTarget) -> Unit)? = null,
    jsConfig:  (KotlinJsTargetDsl.() -> Unit)? = null,
) {
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

    js(IR) {
        browser()
        useEsModules()
        binaries.library()

        jsConfig?.invoke(this)
    }

    wasmJs {
        browser()
        binaries.library()
    }
}