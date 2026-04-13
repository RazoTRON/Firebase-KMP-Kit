package com.firebasekit.messaging

import com.firebasekit.core.Firebase

expect val Firebase.messaging: FirebaseMessaging

interface FirebaseMessaging {
    suspend fun getToken(): String
    suspend fun deleteToken()
    suspend fun subscribeToTopic(topic: String)
    suspend fun unsubscribeFromTopic(topic: String)
}

internal const val UNSUPPORTED_MESSAGE =
    "Firebase Messaging is only supported on Android and iOS in this module"
