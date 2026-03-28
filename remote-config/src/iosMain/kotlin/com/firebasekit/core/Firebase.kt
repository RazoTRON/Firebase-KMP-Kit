package com.firebasekit.core

import com.firebasekit.native.FIRApp
import com.firebasekit.native.FIRRemoteConfig
import com.firebasekit.native.FIRRemoteConfigSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalForeignApi::class)
fun Firebase.initialize(interval: Duration = 60.minutes) {
    FIRApp.configure()
    val settings = FIRRemoteConfigSettings().apply {
        minimumFetchInterval = interval.inWholeSeconds.toDouble()
    }
    FIRRemoteConfig.remoteConfig().setConfigSettings(settings)
}