package com.firebasekit.remoteconfig

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

abstract class JvmRemoteConfig : FirebaseRemoteConfig {
    val settings: Settings = Settings()

    class Settings {
        val refreshInterval: MutableStateFlow<Duration> = MutableStateFlow(60.minutes)
    }
}