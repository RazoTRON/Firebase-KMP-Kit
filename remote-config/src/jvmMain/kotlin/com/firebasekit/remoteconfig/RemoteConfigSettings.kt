package com.firebasekit.remoteconfig

import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun FirebaseRemoteConfig.setConfigSettings(interval: Duration = 60.minutes) {
    remoteConfigJvm.settings.refreshInterval.update { interval }
}