package com.firebasekit.messaging

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSData
import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.messaging.FIRMessagingAPNSTokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class)
class FirebaseMessagingIosTest {

    private class FakeMessagingBridge(
        private val tokenToReturn: String? = "fcm-token",
        private val tokenError: NSError? = null,
        private val deleteError: NSError? = null,
        private val subscribeError: NSError? = null,
        private val unsubscribeError: NSError? = null,
    ) : MessagingBridge {
        var lastSubscribedTopic: String? = null
        var lastUnsubscribedTopic: String? = null
        var lastApnsToken: NSData? = null
        var lastApnsTokenType: FIRMessagingAPNSTokenType? = null

        override fun tokenWithCompletion(completion: (String?, NSError?) -> Unit) =
            completion(tokenToReturn, tokenError)

        override fun deleteTokenWithCompletion(completion: (NSError?) -> Unit) =
            completion(deleteError)

        override fun subscribeToTopic(topic: String, completion: (NSError?) -> Unit) {
            lastSubscribedTopic = topic
            completion(subscribeError)
        }

        override fun unsubscribeFromTopic(topic: String, completion: (NSError?) -> Unit) {
            lastUnsubscribedTopic = topic
            completion(unsubscribeError)
        }

        override fun setApnsToken(apnsToken: NSData) {
            lastApnsToken = apnsToken
            lastApnsTokenType = null
        }

        override fun setApnsToken(apnsToken: NSData, type: FIRMessagingAPNSTokenType) {
            lastApnsToken = apnsToken
            lastApnsTokenType = type
        }
    }

    private fun sut(bridge: FakeMessagingBridge = FakeMessagingBridge()) =
        FirebaseMessagingIos(bridge)

    @Test
    fun getToken_returnsNativeToken_whenAvailable() = runTest {
        assertEquals("fcm-token", sut().getToken())
    }

    @Test
    fun getToken_throws_whenNativeSdkReturnsError() = runTest {
        val error = NSError.errorWithDomain("FirebaseMessaging", 1, null)
        val bridge = FakeMessagingBridge(tokenToReturn = null, tokenError = error)

        val thrown = assertFailsWith<Exception> { sut(bridge).getToken() }
        assertEquals(error.localizedDescription, thrown.message)
    }

    @Test
    fun deleteToken_throws_whenNativeSdkReturnsError() = runTest {
        val error = NSError.errorWithDomain("FirebaseMessaging", 2, null)
        val bridge = FakeMessagingBridge(deleteError = error)

        val thrown = assertFailsWith<Exception> { sut(bridge).deleteToken() }
        assertEquals(error.localizedDescription, thrown.message)
    }

    @Test
    fun subscribeToTopic_passesTopicThroughBridge() = runTest {
        val bridge = FakeMessagingBridge()

        sut(bridge).subscribeToTopic("news")
        assertEquals("news", bridge.lastSubscribedTopic)
    }

    @Test
    fun unsubscribeFromTopic_passesTopicThroughBridge() = runTest {
        val bridge = FakeMessagingBridge()

        sut(bridge).unsubscribeFromTopic("news")
        assertEquals("news", bridge.lastUnsubscribedTopic)
    }

    @Test
    fun setApnsToken_forwardsTokenWithoutExplicitType() {
        val bridge = FakeMessagingBridge()
        val token = NSData()

        sut(bridge).setApnsToken(token)
        assertSame(token, bridge.lastApnsToken)
        assertNull(bridge.lastApnsTokenType)
    }

    @Test
    fun setApnsToken_withType_forwardsTokenAndType() {
        val bridge = FakeMessagingBridge()
        val token = NSData()

        sut(bridge).setApnsToken(token, FIRMessagingAPNSTokenType.FIRMessagingAPNSTokenTypeSandbox)
        assertSame(token, bridge.lastApnsToken)
        assertEquals(
            FIRMessagingAPNSTokenType.FIRMessagingAPNSTokenTypeSandbox,
            bridge.lastApnsTokenType
        )
    }
}
