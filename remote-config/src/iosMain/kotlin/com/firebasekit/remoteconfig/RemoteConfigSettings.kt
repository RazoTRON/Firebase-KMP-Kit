package com.firebasekit.remoteconfig

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfig
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigSettings
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalForeignApi::class)
fun FirebaseRemoteConfig.setConfigSettings(interval: Duration = 60.minutes) {
    val settings = FIRRemoteConfigSettings().apply {
        minimumFetchInterval = interval.inWholeSeconds.toDouble()
    }
    FIRRemoteConfig.remoteConfig().setConfigSettings(settings)
}