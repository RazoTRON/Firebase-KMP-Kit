package com.firebasekit.messaging.bridge.models

import kotlin.js.JsAny
import kotlin.js.JsModule

@JsModule("firebase/messaging")
external interface MessagePayload : JsAny {
    val data: JsAny?
    val notification: NotificationPayload?
    val fcmOptions: String?
    val from: String?
    val collapseKey: String?
    val messageId: String?
}