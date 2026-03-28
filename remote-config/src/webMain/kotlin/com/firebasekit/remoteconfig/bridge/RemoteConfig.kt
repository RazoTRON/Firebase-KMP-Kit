package com.firebasekit.remoteconfig.bridge

import com.firebasekit.core.app
import com.firebasekit.core.utils.awaitJs
import kotlin.js.JsAny

interface RemoteConfig {
    suspend fun fetchAndActivate()
    fun getValue(key: String): JsAny?
    fun getAll(): JsAny?
}

class RemoteConfigBridge : RemoteConfig {
    private val instance: JsAny by lazy {
        val currentApp = app ?: throw Exception("Firebase app is not initialized")
        getRemoteConfig(currentApp)
    }

    override suspend fun fetchAndActivate() {
        fetchAndActivate(instance).awaitJs()
    }

    override fun getValue(key: String): JsAny? = getValue(instance, key)

    override fun getAll(): JsAny? = getAll(instance)
}
