package com.firebasekit.messaging.bridge.models

import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsName

@JsModule("firebase/messaging")
external interface NotificationPayload : JsAny {
    val title: String?
    val body: String?
    val image: String?
    val icon: String?
}