package com.firebasekit.sample

import com.firebasekit.core.Firebase
import com.firebasekit.remoteconfig.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class AppViewModel {
    // Simplified for demonstration purposes, use the appropriate dispatcher.
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val remoteConfigData = flow {
        Firebase.remoteConfig.fetchAndActivate()
        emit(Firebase.remoteConfig.allToJson().toString())
    }
        .catch { emit("Error: ${it.message}") }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = "Loading"
        )
}