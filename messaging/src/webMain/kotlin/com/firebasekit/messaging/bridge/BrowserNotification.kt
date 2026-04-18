package com.firebasekit.messaging.bridge

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.js.JsAny
import kotlin.js.JsName

@JsName("Notification")
external object BrowserNotification : JsAny {
    fun requestPermission(callback: (String) -> Unit)
}

suspend fun requestNotificationPermissions(): Boolean {
    val result = suspendCancellableCoroutine<String> { cont ->
        BrowserNotification.requestPermission { permission ->
            cont.resume(permission)
        }
    }

    return result == "granted"
}