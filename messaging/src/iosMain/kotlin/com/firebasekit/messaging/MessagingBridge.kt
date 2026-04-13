package com.firebasekit.messaging

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.messaging.FIRMessaging
import swiftPMImport.com.firebasekit.messaging.FIRMessagingAPNSTokenType

@OptIn(ExperimentalForeignApi::class)
interface MessagingBridge {
    fun tokenWithCompletion(completion: (String?, NSError?) -> Unit)
    fun deleteTokenWithCompletion(completion: (NSError?) -> Unit)
    fun subscribeToTopic(topic: String, completion: (NSError?) -> Unit)
    fun unsubscribeFromTopic(topic: String, completion: (NSError?) -> Unit)
    fun setApnsToken(apnsToken: NSData)
    fun setApnsToken(apnsToken: NSData, type: FIRMessagingAPNSTokenType)
}

@OptIn(ExperimentalForeignApi::class)
class FIRMessagingBridge(
    private val native: FIRMessaging = FIRMessaging.messaging(),
) : MessagingBridge {
    override fun tokenWithCompletion(completion: (String?, NSError?) -> Unit) =
        native.tokenWithCompletion(completion)

    override fun deleteTokenWithCompletion(completion: (NSError?) -> Unit) =
        native.deleteTokenWithCompletion(completion)

    override fun subscribeToTopic(topic: String, completion: (NSError?) -> Unit) =
        native.subscribeToTopic(topic, completion)

    override fun unsubscribeFromTopic(topic: String, completion: (NSError?) -> Unit) =
        native.unsubscribeFromTopic(topic, completion)

    override fun setApnsToken(apnsToken: NSData) {
        native.APNSToken = apnsToken
    }

    override fun setApnsToken(apnsToken: NSData, type: FIRMessagingAPNSTokenType) {
        native.setAPNSToken(apnsToken, type)
    }
}
