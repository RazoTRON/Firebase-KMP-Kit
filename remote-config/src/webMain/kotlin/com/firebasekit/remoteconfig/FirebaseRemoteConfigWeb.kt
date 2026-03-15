package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.firebasekit.core.app
import com.firebasekit.core.utils.awaitJs
import com.firebasekit.remoteconfig.bridge.fetchAndActivate
import com.firebasekit.remoteconfig.bridge.getAll
import com.firebasekit.remoteconfig.bridge.getRemoteConfig
import com.firebasekit.remoteconfig.bridge.getValue
import com.firebasekit.remoteconfig.bridge.jsValueAsBoolean
import com.firebasekit.remoteconfig.bridge.jsValueAsFlattenJsonString
import com.firebasekit.remoteconfig.bridge.jsValueAsNumber
import com.firebasekit.remoteconfig.bridge.jsValueAsString
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfigWeb

@OptIn(ExperimentalWasmJsInterop::class)
object FirebaseRemoteConfigWeb : FirebaseRemoteConfig {
    private val instance by lazy {
        val app = app ?: throw Exception("Firebase app is not initialized")
        getRemoteConfig(app)
    }

    override suspend fun fetchAndActivate() {
        fetchAndActivate(instance).awaitJs()
    }

    override fun allToJson(): String = jsValueAsFlattenJsonString(getAll(instance))
    override fun getLong(key: String): Long? = getDouble(key)?.toLong()
    override fun getInt(key: String): Int? = getDouble(key)?.toInt()
    override fun getBoolean(key: String): Boolean = jsValueAsBoolean(getValue(key))
    override fun getString(key: String): String = jsValueAsString(getValue(key))
    override fun getDouble(key: String): Double? = jsValueAsNumber(getValue(key)).takeIf { it.isFinite() }

    private fun getValue(key: String): JsAny? = getValue(instance, key)
}