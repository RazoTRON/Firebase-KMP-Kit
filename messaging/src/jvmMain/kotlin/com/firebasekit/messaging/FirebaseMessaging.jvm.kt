package com.firebasekit.messaging

import com.firebasekit.core.Firebase

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessagingJvm()

class FirebaseMessagingJvm : FirebaseMessaging {
    override suspend fun getToken(): String {
        unsupported()
        return ""
    }

    override suspend fun deleteToken() = unsupported()

    override suspend fun subscribeToTopic(topic: String) = unsupported()

    override suspend fun unsubscribeFromTopic(topic: String) = unsupported()

    private fun unsupported() = println(UNSUPPORTED_MESSAGE)
}
