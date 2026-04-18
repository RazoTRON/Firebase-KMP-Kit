package com.firebasekit.messaging

import com.firebasekit.core.Firebase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.messaging.FIRMessagingAPNSTokenType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessagingIos()

@OptIn(ExperimentalForeignApi::class)
class FirebaseMessagingIos(
    private val messaging: MessagingBridge = FIRMessagingBridge(),
) : FirebaseMessaging, ApnsTokenAwareFirebaseMessaging {

    override suspend fun getToken(): String =
        suspendCancellableCoroutine { cont ->
            messaging.tokenWithCompletion { token, error ->
                when {
                    token != null -> cont.resume(token)
                    else -> cont.resumeWithException(messagingException(error, "getToken failed"))
                }
            }
        }

    override suspend fun deleteToken() {
        suspendCancellableCoroutine { cont ->
            messaging.deleteTokenWithCompletion { error ->
                if (error == null) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(messagingException(error, "deleteToken failed"))
                }
            }
        }
    }

    override suspend fun subscribeToTopic(topic: String) {
        suspendCancellableCoroutine { cont ->
            messaging.subscribeToTopic(topic) { error ->
                if (error == null) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(
                        messagingException(error, "subscribeToTopic failed")
                    )
                }
            }
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        suspendCancellableCoroutine { cont ->
            messaging.unsubscribeFromTopic(topic) { error ->
                if (error == null) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(
                        messagingException(error, "unsubscribeFromTopic failed")
                    )
                }
            }
        }
    }

    override fun setApnsToken(apnsToken: NSData) {
        messaging.setApnsToken(apnsToken)
    }

    override fun setApnsToken(apnsToken: NSData, type: FIRMessagingAPNSTokenType) {
        messaging.setApnsToken(apnsToken, type)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal interface ApnsTokenAwareFirebaseMessaging {
    fun setApnsToken(apnsToken: NSData)
    fun setApnsToken(apnsToken: NSData, type: FIRMessagingAPNSTokenType)
}

@OptIn(ExperimentalForeignApi::class)
fun setFirebaseMessagingApnsToken(apnsToken: NSData) {
    val messaging = Firebase.messaging as? ApnsTokenAwareFirebaseMessaging
        ?: throw UnsupportedOperationException(UNSUPPORTED_MESSAGE)
    messaging.setApnsToken(apnsToken)
}

@OptIn(ExperimentalForeignApi::class)
fun setFirebaseMessagingApnsToken(
    apnsToken: NSData,
    type: FIRMessagingAPNSTokenType,
) {
    val messaging = Firebase.messaging as? ApnsTokenAwareFirebaseMessaging
        ?: throw UnsupportedOperationException(UNSUPPORTED_MESSAGE)
    messaging.setApnsToken(apnsToken, type)
}

@OptIn(ExperimentalForeignApi::class)
private fun messagingException(error: NSError?, fallbackMessage: String): Exception =
    Exception(error?.localizedDescription ?: fallbackMessage)
