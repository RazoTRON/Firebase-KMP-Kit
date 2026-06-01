@file:JsModule("firebase/messaging")

package com.firebasekit.messaging.bridge

import com.firebasekit.core.bridge.FirebaseApp
import com.firebasekit.messaging.bridge.models.GetTokenOptions
import com.firebasekit.messaging.bridge.models.MessagePayload
import kotlin.js.JsAny
import kotlin.js.JsBoolean
import kotlin.js.JsModule
import kotlin.js.JsName
import kotlin.js.JsString
import kotlin.js.Promise

@JsName("Messaging")
external interface NativeMessaging : JsAny

@JsName("getMessaging")
external fun nativeGetMessaging(): NativeMessaging
@JsName("getMessaging")
external fun nativeGetMessaging(app: FirebaseApp): NativeMessaging

@JsName("getToken")
external fun nativeGetToken(messaging: NativeMessaging): Promise<JsString>
@JsName("getToken")
external fun nativeGetToken(messaging: NativeMessaging, options: GetTokenOptions): Promise<JsString>

@JsName("onMessage")
external fun nativeOnMessage(messaging: NativeMessaging, nextOrObserver: (MessagePayload) -> Unit)

@JsName("deleteToken")
external fun nativeDeleteToken(messaging: NativeMessaging): Promise<JsBoolean>