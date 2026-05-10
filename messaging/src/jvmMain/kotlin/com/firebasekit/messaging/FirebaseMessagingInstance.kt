package com.firebasekit.messaging

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm
import com.firebasekit.messaging.FirebaseMessaging.Companion.DEFAULT_CACHE_FILE_PATH
import com.firebasekit.messaging.FirebaseMessaging.Companion.DEFAULT_REFRESH_DURATION
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


actual val Firebase.messaging: FirebaseMessaging by lazy { FirebaseMessagingJvm() }

internal class FirebaseMessagingJvm(
    private val bridge: FcmBrowserBridge = LocalFcmBrowserBridge(),
) : FirebaseMessaging {
    override var refreshTokenDuration: Duration = DEFAULT_REFRESH_DURATION
    override var cacheTokenPath: String = DEFAULT_CACHE_FILE_PATH

    private val tokenCache: FirebaseMessagingTokenCache = FirebaseMessagingTokenCache(
        cacheFile = { File(cacheTokenPath) },
        refreshDuration = { refreshTokenDuration }
    )

    override suspend fun getToken(): String {
        tokenCache.getValidToken()?.let { return it }

        return bridge.getToken(firebaseConfig())
            .also { token -> tokenCache.save(token) }
    }

    override suspend fun deleteToken() {
        bridge.deleteToken(firebaseConfig())
        tokenCache.clear()
    }

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
}
