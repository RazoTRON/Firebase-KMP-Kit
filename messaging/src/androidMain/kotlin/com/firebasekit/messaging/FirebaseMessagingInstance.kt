package com.firebasekit.messaging

import com.firebasekit.core.Firebase
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging as AndroidFirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessagingAndroid()

class FirebaseMessagingAndroid(
    private val messaging: AndroidFirebaseMessaging = AndroidFirebaseMessaging.getInstance(),
) : FirebaseMessaging {
    override suspend fun getToken(): String = messaging.token.awaitCompletion()

    override suspend fun deleteToken() {
        messaging.deleteToken().awaitCompletion()
    }

    override suspend fun subscribeToTopic(topic: String) {
        messaging.subscribeToTopic(topic).awaitCompletion()
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic).awaitCompletion()
    }
}

private suspend fun <T> Task<T>.awaitCompletion(): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Task $this failed"))
            }
        }
    }
