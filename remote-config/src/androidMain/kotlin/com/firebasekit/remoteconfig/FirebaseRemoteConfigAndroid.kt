package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase as AndroidFirebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig as AndroidRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.remoteConfig
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.json.JSONObject

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfigAndroid()

class FirebaseRemoteConfigAndroid(
    remoteConfig: AndroidRemoteConfig = AndroidFirebase.remoteConfig,
) : FirebaseRemoteConfig {
    private val instance: AndroidRemoteConfig by lazy { remoteConfig }

    override suspend fun fetchAndActivate() {
        instance.fetchAndActivate().awaitCompletion()
    }

    override fun getBoolean(key: String): Boolean = instance.getBoolean(key)
    override fun getString(key: String): String = instance.getString(key)
    override fun getDouble(key: String): Double = instance.getDouble(key)
    override fun getLong(key: String): Long = instance.getLong(key)
    override fun getInt(key: String): Int = getLong(key).toInt()

    override fun allToJson(): String {
        val entries: Map<String, String> = instance.all.mapValues { it.value.asString() }
        return Json.encodeToString(entries)
    }
}

private suspend fun <T> Task<T>.awaitCompletion(): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Task $this failed"))
            }
        }
    }

