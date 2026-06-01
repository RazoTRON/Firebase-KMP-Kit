package com.firebasekit.sample

import com.firebasekit.BuildConfig
import com.firebasekit.core.Firebase
import com.firebasekit.messaging.messaging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SAMPLE_PUSH_TITLE = "Firebase"
private const val SAMPLE_PUSH_BODY = "Test push notification"

data class MessagingUiState(
    val token: String = "Refreshing FCM token…",
    val statusMessage: String = "",
    val isRefreshingToken: Boolean = true,
    val isSendingPush: Boolean = false,
    val canSendPush: Boolean = false,
)

class MessagingViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sender by lazy { PushNotificationSender() }
    private val firebaseMessaging = Firebase.messaging

    private val _uiState = MutableStateFlow(
        MessagingUiState(
            isRefreshingToken = true,
            statusMessage = "Refreshing FCM token…",
        )
    )
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    init {
        checkCanSendPush {
            refreshToken()
        }
    }

    fun checkCanSendPush(onSuccess: () -> Unit) {
        if (BuildConfig.FIREBASE_FCM_ACCESS_TOKEN.isBlank()) {
            _uiState.update {
                it.copy(
                    isRefreshingToken = false,
                    canSendPush = false,
                    statusMessage = "Add FIREBASE_FCM_ACCESS_TOKEN to local.properties to enable the self-push sample button.",
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    canSendPush = true,
                    statusMessage = "Ready to send a sample push notification to this device."
                )
            }
            onSuccess()
        }
    }

    fun refreshToken() {
        scope.launch {
            _uiState.update {
                it.copy(
                    isRefreshingToken = true,
                    canSendPush = false,
                    statusMessage = "Refreshing FCM token…",
                )
            }

            val result =  runCatching { firebaseMessaging.getToken() }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { token ->
                        current.copy(
                            token = token,
                            isRefreshingToken = false,
                            canSendPush = BuildConfig.FIREBASE_FCM_ACCESS_TOKEN.isNotBlank(),
                            statusMessage = "Ready to send a sample push notification to this device."
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            token = "",
                            isRefreshingToken = false,
                            canSendPush = false,
                            statusMessage = error.message ?: "Failed to load the FCM token.",
                        )
                    }
                )
            }
        }
    }

    fun sendPushToSelf() {
        if (uiState.value.canSendPush.not() || uiState.value.isRefreshingToken) {
            return
        }

        val token = uiState.value.token

        if (token.isBlank()) {
            _uiState.update {
                it.copy(statusMessage = "Refresh the FCM token before sending a sample push.")
            }
            return
        }

        scope.launch {
            _uiState.update {
                it.copy(
                    isSendingPush = true,
                    statusMessage = "Sending a push notification to this token…",
                )
            }

            val result = runCatching { sender.sendToToken(token) }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { messageName ->
                        current.copy(
                            isSendingPush = false,
                            statusMessage = "Push accepted by FCM: $messageName",
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSendingPush = false,
                            statusMessage = error.message ?: "Failed to send the push notification.",
                        )
                    }
                )
            }
        }
    }
}

/**
 * Sample-only sender for self-testing FCM with a short-lived OAuth access token from local.properties.
 */
private class PushNotificationSender {
    private val client = HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun sendToToken(token: String): String {
        val accessToken = BuildConfig.FIREBASE_FCM_ACCESS_TOKEN.trim()
        require(accessToken.isNotEmpty()) {
            "Add FIREBASE_FCM_ACCESS_TOKEN to local.properties before using this sample."
        }

        val response = client.post(
            "https://fcm.googleapis.com/v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/messages:send"
        ) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(
                FcmSendRequest(
                    message = FcmMessage(
                        token = token,
                        notification = FcmNotification(
                            title = SAMPLE_PUSH_TITLE,
                            body = SAMPLE_PUSH_BODY,
                        ),
                        data = mapOf("source" to "firebase"),
                    )
                )
            )
        }

        return response.body<FcmSendResponse>().name
    }
}

@Serializable
private data class FcmSendRequest(
    val message: FcmMessage,
)

@Serializable
private data class FcmMessage(
    val token: String,
    val notification: FcmNotification,
    val data: Map<String, String> = emptyMap(),
)

@Serializable
private data class FcmNotification(
    val title: String,
    val body: String,
)

@Serializable
private data class FcmSendResponse(
    val name: String,
)
