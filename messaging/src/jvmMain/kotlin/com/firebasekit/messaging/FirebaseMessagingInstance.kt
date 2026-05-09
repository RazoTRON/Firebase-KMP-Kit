package com.firebasekit.messaging

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm


actual val Firebase.messaging: FirebaseMessaging by lazy { FirebaseMessagingJvm() }

internal class FirebaseMessagingJvm(
    private val bridge: FcmBrowserBridge = LocalFcmBrowserBridge(),
) : FirebaseMessaging {
    override suspend fun getToken(): String = bridge.getToken(firebaseConfig())

    override suspend fun deleteToken() = bridge.deleteToken(firebaseConfig())

    override suspend fun subscribeToTopic(topic: String) = unsupported()

    override suspend fun unsubscribeFromTopic(topic: String) = unsupported()

    override fun onMessage(block: (payload: String) -> Unit) = bridge.onMessage(block)

    private fun firebaseConfig(): DesktopMessagingConfig {
        val apiKey = FirebaseJvm.apiKey ?: throw IllegalStateException("Firebase app is not initialized")
        val projectId = FirebaseJvm.projectId ?: throw IllegalStateException("Firebase project ID is not set")
        val appId = FirebaseJvm.appId ?: throw IllegalStateException("Firebase app ID is not set")
        val messagingSenderId = FirebaseJvm.messagingSenderId
            ?: throw IllegalStateException("Firebase Messaging sender ID is not set")
        val webVapidKey = FirebaseJvm.webVapidKey
            ?: throw IllegalStateException("Firebase Web VAPID key is not set")

        return DesktopMessagingConfig(
            apiKey = apiKey,
            authDomain = FirebaseJvm.authDomain ?: "$projectId.firebaseapp.com",
            projectId = projectId,
            storageBucket = FirebaseJvm.storageBucket,
            messagingSenderId = messagingSenderId,
            appId = appId,
            measurementId = FirebaseJvm.measurementId,
            webVapidKey = webVapidKey,
        )
    }

    private fun unsupported(): Nothing = throw UnsupportedOperationException(UNSUPPORTED_MESSAGE)
}
