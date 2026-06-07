package com.firebasekit.crashlytics

import platform.Foundation.NSError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FirebaseCrashlyticsIosTest {
    private class FakeCrashlyticsBridge : CrashlyticsBridge {
        var collectionEnabled: Boolean? = null
        var crashedOnPreviousExecution = false
        var userId: String? = null
        val customValues = linkedMapOf<String, Any>()
        var customKeys: Map<Any?, *>? = null
        var logMessage: String? = null
        var recordedError: NSError? = null
        var recordedErrorUserInfo: Map<Any?, *>? = null
        var sendUnsentReportsCalls = 0
        var deleteUnsentReportsCalls = 0

        override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
            collectionEnabled = enabled
        }

        override fun didCrashOnPreviousExecution(): Boolean {
            return crashedOnPreviousExecution
        }

        override fun setUserId(userId: String) {
            this.userId = userId
        }

        override fun setCustomValue(value: Any, key: String) {
            customValues[key] = value
        }

        override fun setCustomKeysAndValues(keys: Map<Any?, *>) {
            customKeys = keys
        }

        override fun log(message: String) {
            logMessage = message
        }

        override fun recordError(error: NSError) {
            recordedError = error
        }

        override fun recordError(error: NSError, userInfo: Map<Any?, *>) {
            recordedError = error
            recordedErrorUserInfo = userInfo
        }

        override fun sendUnsentReports() {
            sendUnsentReportsCalls += 1
        }

        override fun deleteUnsentReports() {
            deleteUnsentReportsCalls += 1
        }
    }

    private fun sut(bridge: FakeCrashlyticsBridge = FakeCrashlyticsBridge()) =
        FirebaseCrashlyticsIos(bridge)

    @Test
    fun setCrashlyticsCollectionEnabled_delegatesToBridge() {
        val bridge = FakeCrashlyticsBridge()

        sut(bridge).setCrashlyticsCollectionEnabled(false)

        assertEquals(false, bridge.collectionEnabled)
    }

    @Test
    fun didCrashOnPreviousExecution_returnsBridgeValue() {
        val bridge = FakeCrashlyticsBridge().apply { crashedOnPreviousExecution = true }

        assertTrue(sut(bridge).didCrashOnPreviousExecution())
    }

    @Test
    fun setUserId_delegatesToBridge() {
        val bridge = FakeCrashlyticsBridge()

        sut(bridge).setUserId("user-42")

        assertEquals("user-42", bridge.userId)
    }

    @Test
    fun setCustomKey_delegatesSupportedPrimitiveTypes() {
        val bridge = FakeCrashlyticsBridge()
        val crashlytics = sut(bridge)

        crashlytics.setCustomKey("string", "value")
        crashlytics.setCustomKey("boolean", true)
        crashlytics.setCustomKey("double", 2.5)
        crashlytics.setCustomKey("float", 1.5f)
        crashlytics.setCustomKey("int", 7)
        crashlytics.setCustomKey("long", 9L)

        assertEquals("value", bridge.customValues["string"])
        assertEquals(true, bridge.customValues["boolean"])
        assertEquals(2.5, bridge.customValues["double"])
        assertEquals(1.5f, bridge.customValues["float"])
        assertEquals(7, bridge.customValues["int"])
        assertEquals(9L, bridge.customValues["long"])
    }

    @Test
    fun setCustomKeys_convertsKeysToNativeUserInfo() {
        val bridge = FakeCrashlyticsBridge()

        sut(bridge).setCustomKeys(
            CrashlyticsKeys().apply {
                put("screen", "checkout")
                put("retrying", false)
                put("price", 12.5)
                put("score", 3.5f)
                put("items", 2)
                put("attempt", 4L)
            }
        )

        assertEquals("checkout", bridge.customKeys?.get("screen"))
        assertEquals(false, bridge.customKeys?.get("retrying"))
        assertEquals(12.5, bridge.customKeys?.get("price"))
        assertEquals(3.5f, bridge.customKeys?.get("score"))
        assertEquals(2, bridge.customKeys?.get("items"))
        assertEquals(4L, bridge.customKeys?.get("attempt"))
    }

    @Test
    fun log_delegatesToBridge() {
        val bridge = FakeCrashlyticsBridge()

        sut(bridge).log("checkout failed")

        assertEquals("checkout failed", bridge.logMessage)
    }

    @Test
    fun recordException_convertsThrowableToNSError() {
        val bridge = FakeCrashlyticsBridge()
        val exception = IllegalStateException("boom")

        sut(bridge).recordException(exception)

        val error = assertNotNull(bridge.recordedError)
        assertEquals("com.firebasekit.crashlytics.KotlinThrowable", error.domain)
        assertTrue(error.localizedDescription.contains("boom"))
    }

    @Test
    fun recordExceptionWithKeys_convertsThrowableAndUserInfo() {
        val bridge = FakeCrashlyticsBridge()
        val exception = IllegalStateException("boom")

        sut(bridge).recordException(
            throwable = exception,
            keys = CrashlyticsKeys().apply { put("fatal", false) },
        )

        assertNotNull(bridge.recordedError)
        assertEquals(false, bridge.recordedErrorUserInfo?.get("fatal"))
    }

    @Test
    fun sendAndDeleteUnsentReports_delegateToBridge() {
        val bridge = FakeCrashlyticsBridge()
        val crashlytics = sut(bridge)

        crashlytics.sendUnsentReports()
        crashlytics.deleteUnsentReports()

        assertEquals(1, bridge.sendUnsentReportsCalls)
        assertEquals(1, bridge.deleteUnsentReportsCalls)
    }
}
