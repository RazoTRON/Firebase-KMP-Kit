package com.firebasekit.messaging

import com.firebasekit.core.Firebase

expect val Firebase.messaging: FirebaseMessaging

expect interface FirebaseMessaging {
    suspend fun getToken(): String
    suspend fun deleteToken()
}

internal const val UNSUPPORTED_MESSAGE =
    "Firebase Messaging topic subscriptions are only supported on Android and iOS in this module"
