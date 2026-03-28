package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase

expect val Firebase.remoteConfig: FirebaseRemoteConfig

interface FirebaseRemoteConfig {
    suspend fun fetchAndActivate()
    fun getBoolean(key: String): Boolean?
    fun getString(key: String): String?
    fun getDouble(key: String): Double?
    fun getLong(key: String): Long?
    fun getInt(key: String): Int?
    fun allToJson(): String?
}