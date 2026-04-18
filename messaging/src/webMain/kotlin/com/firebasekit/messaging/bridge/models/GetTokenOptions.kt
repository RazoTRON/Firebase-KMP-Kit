package com.firebasekit.messaging.bridge.models

import kotlin.js.JsAny
import kotlin.js.JsModule

@JsModule("firebase/messaging")
external interface GetTokenOptions : JsAny {
    var vapidKey: String?
}