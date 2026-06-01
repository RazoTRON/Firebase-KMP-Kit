package com.firebasekit.messaging

import com.firebasekit.messaging.bridge.models.MessagePayload

actual interface FirebaseMessaging {
    actual suspend fun getToken(): String
    actual suspend fun deleteToken()
    fun onMessage(block: (payload: MessagePayload) -> Unit)
}