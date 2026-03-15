package com.firebasekit.core

import com.firebasekit.native.FIRRemoteConfig
import com.firebasekit.native.FIRRemoteConfigSettings
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun Firebase.initialize(intervalSeconds: Int) {
    val settings = FIRRemoteConfigSettings().apply {
        minimumFetchInterval = intervalSeconds.toDouble()
    }
    FIRRemoteConfig.remoteConfig().setConfigSettings(settings)
}

