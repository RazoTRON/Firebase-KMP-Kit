package com.firebasekit.messaging

import com.firebasekit.core.Firebase

expect val Firebase.messaging: FirebaseMessaging

expect interface FirebaseMessaging {
    suspend fun getToken(): String
    suspend fun deleteToken()
}

internal const val UNSUPPORTED_MESSAGE =
    "Firebase Messaging is only supported on Android, iOS, JS, and Wasm in this module"
