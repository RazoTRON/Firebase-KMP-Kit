package com.firebasekit.messaging.bridge

import com.firebasekit.core.bridge.FirebaseApp
import com.firebasekit.messaging.bridge.models.GetTokenOptions
import com.firebasekit.messaging.bridge.models.MessagePayload
import kotlin.js.JsBoolean
import kotlin.js.JsString
import kotlin.js.Promise

interface Messaging {
    fun getMessaging(): NativeMessaging
    fun getMessaging(app: FirebaseApp): NativeMessaging
    fun getToken(messaging: NativeMessaging): Promise<JsString>
    fun getToken(messaging: NativeMessaging, options: GetTokenOptions): Promise<JsString>
    fun onMessage(messaging: NativeMessaging, nextOrObserver: (MessagePayload) -> Unit)
    fun deleteToken(messaging: NativeMessaging): Promise<JsBoolean>
}

internal class FirebaseMessagingBridge : Messaging {
    override fun getMessaging(): NativeMessaging = nativeGetMessaging()
    override fun getMessaging(app: FirebaseApp): NativeMessaging = nativeGetMessaging(app)
    override fun getToken(messaging: NativeMessaging): Promise<JsString> = nativeGetToken(messaging)
    override fun getToken(messaging: NativeMessaging, options: GetTokenOptions): Promise<JsString> = nativeGetToken(messaging, options)
    override fun onMessage(messaging: NativeMessaging, nextOrObserver: (MessagePayload) -> Unit) = nativeOnMessage(messaging, nextOrObserver)
    override fun deleteToken(messaging: NativeMessaging): Promise<JsBoolean> = nativeDeleteToken(messaging)
}