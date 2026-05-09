package com.firebasekit.messaging

import com.firebasekit.core.FirebaseJvm
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FirebaseMessagingJvmTest {

    private val bridge = FakeFcmBrowserBridge()

    private fun sut() = FirebaseMessagingJvm(bridge)

    @BeforeTest
    fun setup() {
        setFirebaseJvmField("apiKey", "test-api-key")
        setFirebaseJvmField("projectId", "test-project-id")
        setFirebaseJvmField("appId", "test-app-id")
        setFirebaseJvmField("authDomain", "test-project.firebaseapp.com")
        setFirebaseJvmField("storageBucket", "test-project.firebasestorage.app")
        setFirebaseJvmField("messagingSenderId", "1234567890")
        setFirebaseJvmField("webVapidKey", "test-vapid-key")
        setFirebaseJvmField("measurementId", "G-TEST")
    }

    @AfterTest
    fun teardown() {
        setFirebaseJvmField("apiKey", null)
        setFirebaseJvmField("projectId", null)
        setFirebaseJvmField("appId", null)
        setFirebaseJvmField("authDomain", null)
        setFirebaseJvmField("storageBucket", null)
        setFirebaseJvmField("messagingSenderId", null)
        setFirebaseJvmField("webVapidKey", null)
        setFirebaseJvmField("measurementId", null)
    }

    @Test
    fun getToken_delegatesToBrowserBridge_withFirebaseConfig() = runTest {
        val token = sut().getToken()

        assertEquals("test-token", token)
        assertEquals(
            DesktopMessagingConfig(
                apiKey = "test-api-key",
                authDomain = "test-project.firebaseapp.com",
                projectId = "test-project-id",
                storageBucket = "test-project.firebasestorage.app",
                messagingSenderId = "1234567890",
                appId = "test-app-id",
                measurementId = "G-TEST",
                webVapidKey = "test-vapid-key",
            ),
            bridge.lastTokenConfig,
        )
    }

    @Test
    fun deleteToken_delegatesToBrowserBridge_withFirebaseConfig() = runTest {
        sut().deleteToken()

        assertEquals("test-api-key", bridge.lastDeleteConfig?.apiKey)
        assertTrue(bridge.deleteCalled)
    }

    @Test
    fun getToken_throwsWhenMessagingSenderIdIsMissing() = runTest {
        setFirebaseJvmField("messagingSenderId", null)

        val error = try {
            sut().getToken()
            null
        } catch (error: Throwable) {
            error
        }
        assertNotNull(error)
        assertIs<IllegalStateException>(error)
        assertEquals("Firebase Messaging sender ID is not set", error.message)
    }

    @Test
    fun subscribeToTopic_throwsUnsupportedOperation() = runTest {
        val error = try {
            sut().subscribeToTopic("news")
            null
        } catch (error: Throwable) {
            error
        }
        assertNotNull(error)
        assertIs<UnsupportedOperationException>(error)
        assertEquals(UNSUPPORTED_MESSAGE, error.message)
    }

    @Test
    fun unsubscribeFromTopic_throwsUnsupportedOperation() = runTest {
        val error = try {
            sut().unsubscribeFromTopic("news")
            null
        } catch (error: Throwable) {
            error
        }
        assertNotNull(error)
        assertIs<UnsupportedOperationException>(error)
        assertEquals(UNSUPPORTED_MESSAGE, error.message)
    }

    private class FakeFcmBrowserBridge : FcmBrowserBridge {
        var lastTokenConfig: DesktopMessagingConfig? = null
        var lastDeleteConfig: DesktopMessagingConfig? = null
        var deleteCalled = false

        override suspend fun getToken(config: DesktopMessagingConfig): String {
            lastTokenConfig = config
            return "test-token"
        }

        override suspend fun deleteToken(config: DesktopMessagingConfig) {
            lastDeleteConfig = config
            deleteCalled = true
        }

        override fun onMessage(block: (payload: String) -> Unit) = Unit
    }

    private fun setFirebaseJvmField(name: String, value: Any?) {
        val field = FirebaseJvm::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(FirebaseJvm, value)
    }
}
