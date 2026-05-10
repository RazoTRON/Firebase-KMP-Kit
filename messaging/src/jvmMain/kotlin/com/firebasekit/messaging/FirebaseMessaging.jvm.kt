package com.firebasekit.messaging

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

actual interface FirebaseMessaging {
    actual suspend fun getToken(): String
    actual suspend fun deleteToken()
    fun onMessage(block: (payload: String) -> Unit)

    var refreshTokenDuration: Duration
    var cacheTokenPath: String

    companion object {
        const val DEFAULT_CACHE_FILE_PATH = "cache/firebase_messaging_token"
        val DEFAULT_REFRESH_DURATION: Duration = 30.days
    }
}