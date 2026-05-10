package com.firebasekit.messaging

import com.firebasekit.core.FirebaseJvm
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FirebaseMessagingJvmTest {

    private val bridge = FakeFcmBrowserBridge()
    private val tokenCacheFiles = mutableListOf<File>()
    private var nowEpochSeconds = 1_700_000_000L

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
        nowEpochSeconds = 1_700_000_000L
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
        tokenCacheFiles.forEach { it.delete() }
        tokenCacheFiles.clear()
    }

    @Test
    fun getToken_delegatesToBrowserBridge_withFirebaseConfig() = runTest {
        val token = sut().getToken()

        assertEquals("test-token", token)
        assertEquals(1, bridge.tokenRequests)
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
    fun getToken_returnsCachedTokenWithinDefaultRefreshDuration() = runTest {
        val messaging = sut()

        assertEquals("test-token", messaging.getToken())

        bridge.tokenToReturn = "new-token"
        nowEpochSeconds += 29.days.inWholeSeconds

        assertEquals("test-token", messaging.getToken())
        assertEquals(1, bridge.tokenRequests)
    }

    @Test
    fun getToken_refreshesCachedTokenAfterDefaultRefreshDuration() = runTest {
        val messaging = sut()

        assertEquals("test-token", messaging.getToken())

        bridge.tokenToReturn = "refreshed-token"
        nowEpochSeconds += 30.days.inWholeSeconds

        assertEquals("refreshed-token", messaging.getToken())
        assertEquals(2, bridge.tokenRequests)
    }

    @Test
    fun getToken_savesTokenDateAndRefreshDuration() = runTest {
        val cacheFile = tokenCacheFile()

        sut().getToken()

        val cached = cacheFile.readText()
        assertTrue(cached.contains("\"token\":\"test-token\""))
        assertTrue(cached.contains("\"savedAtEpochSeconds\":1700000000"))
        assertTrue(cached.contains("\"refreshDurationSeconds\":2592000"))
    }

    @Test
    fun deleteToken_delegatesToBrowserBridge_withFirebaseConfig() = runTest {
        sut().deleteToken()

        assertEquals("test-api-key", bridge.lastDeleteConfig?.apiKey)
        assertTrue(bridge.deleteCalled)
    }

    @Test
    fun deleteToken_clearsCachedTokenAfterBridgeDeleteSucceeds() = runTest {
        val cacheFile = tokenCacheFile()
        val messaging = sut()

        messaging.getToken()
        assertTrue(cacheFile.exists())

        messaging.deleteToken()

        assertTrue(cacheFile.exists().not())
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

    private class FakeFcmBrowserBridge : FcmBrowserBridge {
        var lastTokenConfig: DesktopMessagingConfig? = null
        var lastDeleteConfig: DesktopMessagingConfig? = null
        var deleteCalled = false
        var tokenRequests = 0
        var tokenToReturn = "test-token"

        override suspend fun getToken(config: DesktopMessagingConfig): String {
            lastTokenConfig = config
            tokenRequests += 1
            return tokenToReturn
        }

        override suspend fun deleteToken(config: DesktopMessagingConfig) {
            lastDeleteConfig = config
            deleteCalled = true
        }

        override fun onMessage(block: (payload: String) -> Unit) = Unit
    }

    private fun tokenCacheFile(): File {
        val file = File.createTempFile("firebase-messaging-token", ".json")
        file.delete()
        tokenCacheFiles += file
        return file
    }

    private fun setFirebaseJvmField(name: String, value: Any?) {
        val field = FirebaseJvm::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(FirebaseJvm, value)
    }
}
