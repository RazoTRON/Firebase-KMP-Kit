package com.firebasekit.messaging

import com.firebasekit.core.Firebase
import com.firebasekit.core.app
import com.firebasekit.core.common.JSBuilder
import com.firebasekit.core.common.utils.awaitJs
import com.firebasekit.messaging.bridge.FirebaseMessagingBridge
import com.firebasekit.messaging.bridge.Messaging
import com.firebasekit.messaging.bridge.NativeMessaging
import com.firebasekit.messaging.bridge.models.MessagePayload
import com.firebasekit.messaging.bridge.nativeOnMessage

actual val Firebase.messaging: FirebaseMessaging by lazy { FirebaseMessagingWeb() }

class FirebaseMessagingWeb(private val bridge: Messaging = FirebaseMessagingBridge()) : FirebaseMessaging {
    private val instance: NativeMessaging by lazy {
        val currentApp = app ?: throw Exception("Firebase app is not initialized")
        bridge.getMessaging(currentApp)
    }

    override suspend fun getToken(): String {
        val token = bridge.getToken(
            messaging = instance,
            options = JSBuilder.build { this.vapidKey = vapidKey }
        ).awaitJs()

        return token.toString()
    }

    override suspend fun deleteToken() { bridge.deleteToken(instance) }

    override fun onMessage(block: (payload: MessagePayload) -> Unit) {
        nativeOnMessage(instance, block)
    }
}