package com.firebasekit.remoteconfig

import com.firebasekit.native.FIRRemoteConfig
import com.firebasekit.native.FIRRemoteConfigSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalForeignApi::class)
fun FirebaseRemoteConfig.setConfigSettings(interval: Duration = 60.minutes) {
    val settings = FIRRemoteConfigSettings().apply {
        minimumFetchInterval = interval.inWholeSeconds.toDouble()
    }
    FIRRemoteConfig.remoteConfig().setConfigSettings(settings)
}