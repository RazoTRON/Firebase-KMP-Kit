package com.firebasekit.messaging

import java.net.URI
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

internal interface FcmBrowserBridge {
    suspend fun getToken(config: DesktopMessagingConfig): String
    suspend fun deleteToken(config: DesktopMessagingConfig)
    fun onMessage(block: (payload: String) -> Unit)
}

internal class LocalFcmBrowserBridge(
    private val browserOpener: BrowserOpener = SystemBrowserOpener(),
) : FcmBrowserBridge {
    private val session = UUID.randomUUID().toString()
    private val messageCallbacks = CopyOnWriteArrayList<(String) -> Unit>()

    @Volatile
    private var currentConfig: DesktopMessagingConfig? = null

    @Volatile
    private var tokenRequest: CompletableDeferred<String>? = null

    @Volatile
    private var deleteRequest: CompletableDeferred<Unit>? = null

    private val server = FcmBrowserBridgeServer(
        session = session,
        configProvider = { currentConfig ?: error("Firebase app is not initialized") },
        onToken = { token -> tokenRequest?.complete(token) },
        onDelete = { deleteRequest?.complete(Unit) },
        onMessage = { body -> messageCallbacks.forEach { callback -> callback(body) } },
        onError = ::completePendingRequestsWithError,
    )


    override suspend fun getToken(config: DesktopMessagingConfig): String {
        currentConfig = config
        val deferred = CompletableDeferred<String>()
        tokenRequest = deferred

        browserOpener.open(URI("${server.start()}/?session=$session&action=token"))

        return withTimeout(REGISTRATION_TIMEOUT) { deferred.await() }
    }

    override suspend fun deleteToken(config: DesktopMessagingConfig) {
        currentConfig = config
        val deferred = CompletableDeferred<Unit>()
        deleteRequest = deferred

        browserOpener.open(URI("${server.start()}/?session=$session&action=delete"))

        withTimeout(REGISTRATION_TIMEOUT) { deferred.await() }
    }

    override fun onMessage(block: (payload: String) -> Unit) {
        messageCallbacks += block
    }

    private fun completePendingRequestsWithError(message: String) {
        val error = IllegalStateException(message)
        tokenRequest?.completeExceptionally(error)
        deleteRequest?.completeExceptionally(error)
    }

    companion object {
        private val REGISTRATION_TIMEOUT = 2.minutes
    }
}
