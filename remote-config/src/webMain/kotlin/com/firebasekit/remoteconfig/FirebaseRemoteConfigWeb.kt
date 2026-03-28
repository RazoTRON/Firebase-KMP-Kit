package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.firebasekit.remoteconfig.bridge.RemoteConfigBridge
import com.firebasekit.remoteconfig.bridge.RemoteConfig
import com.firebasekit.remoteconfig.bridge.jsValueAsBoolean
import com.firebasekit.remoteconfig.bridge.jsValueAsFlattenJsonString
import com.firebasekit.remoteconfig.bridge.jsValueAsNumber
import com.firebasekit.remoteconfig.bridge.jsValueAsString
import kotlin.js.ExperimentalWasmJsInterop

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfigWeb()

@OptIn(ExperimentalWasmJsInterop::class)
class FirebaseRemoteConfigWeb(bridge: RemoteConfig = RemoteConfigBridge()) : FirebaseRemoteConfig {
    private val bridge: RemoteConfig by lazy { bridge }

    override suspend fun fetchAndActivate() = this.bridge.fetchAndActivate()

    override fun allToJson(): String = jsValueAsFlattenJsonString(bridge.getAll())
    override fun getLong(key: String): Long? = getDouble(key)?.toLong()
    override fun getInt(key: String): Int? = getDouble(key)?.toInt()
    override fun getBoolean(key: String): Boolean = jsValueAsBoolean(bridge.getValue(key))
    override fun getString(key: String): String = jsValueAsString(bridge.getValue(key))
    override fun getDouble(key: String): Double? = jsValueAsNumber(bridge.getValue(key)).takeIf { it.isFinite() }
}