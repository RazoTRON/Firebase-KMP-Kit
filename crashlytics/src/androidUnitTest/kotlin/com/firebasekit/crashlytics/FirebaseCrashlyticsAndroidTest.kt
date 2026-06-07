package com.firebasekit.crashlytics

import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics as AndroidFirebaseCrashlytics
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirebaseCrashlyticsAndroidTest {
    private val nativeCrashlytics: AndroidFirebaseCrashlytics = mockk(relaxUnitFun = true)

    private fun sut() = FirebaseCrashlyticsAndroid(nativeCrashlytics)

    @Test
    fun setCrashlyticsCollectionEnabled_delegatesToNativeSdk() {
        sut().setCrashlyticsCollectionEnabled(false)

        verify(exactly = 1) { nativeCrashlytics.isCrashlyticsCollectionEnabled = false }
    }

    @Test
    fun didCrashOnPreviousExecution_returnsNativeValue() {
        every { nativeCrashlytics.didCrashOnPreviousExecution() } returns true

        assertTrue(sut().didCrashOnPreviousExecution())
    }

    @Test
    fun setUserId_delegatesToNativeSdk() {
        sut().setUserId("user-42")

        verify(exactly = 1) { nativeCrashlytics.setUserId("user-42") }
    }

    @Test
    fun setCustomKey_delegatesSupportedPrimitiveTypes() {
        val crashlytics = sut()

        crashlytics.setCustomKey("string", "value")
        crashlytics.setCustomKey("boolean", true)
        crashlytics.setCustomKey("double", 2.5)
        crashlytics.setCustomKey("float", 1.5f)
        crashlytics.setCustomKey("int", 7)
        crashlytics.setCustomKey("long", 9L)

        verify(exactly = 1) { nativeCrashlytics.setCustomKey("string", "value") }
        verify(exactly = 1) { nativeCrashlytics.setCustomKey("boolean", true) }
        verify(exactly = 1) { nativeCrashlytics.setCustomKey("double", 2.5) }
        verify(exactly = 1) { nativeCrashlytics.setCustomKey("float", 1.5f) }
        verify(exactly = 1) { nativeCrashlytics.setCustomKey("int", 7) }
        verify(exactly = 1) { nativeCrashlytics.setCustomKey("long", 9L) }
    }

    @Test
    fun setCustomKeys_translatesKeysToNativeContainer() {
        val keysSlot = slot<CustomKeysAndValues>()
        every { nativeCrashlytics.setCustomKeys(capture(keysSlot)) } just runs

        sut().setCustomKeys(
            CrashlyticsKeys().apply {
                put("screen", "checkout")
                put("retrying", false)
                put("price", 12.5)
                put("score", 3.5f)
                put("items", 2)
                put("attempt", 4L)
            }
        )

        verify(exactly = 1) { nativeCrashlytics.setCustomKeys(any()) }
        val keyValues = keysSlot.captured.toKeyValuesMap()
        assertTrue(keyValues.containsValue("checkout"))
        assertTrue(keyValues.containsValue("false"))
        assertTrue(keyValues.containsValue("12.5"))
        assertTrue(keyValues.containsValue("3.5"))
        assertTrue(keyValues.containsValue("2"))
        assertTrue(keyValues.containsValue("4"))
    }

    @Test
    fun log_delegatesToNativeSdk() {
        sut().log("checkout failed")

        verify(exactly = 1) { nativeCrashlytics.log("checkout failed") }
    }

    @Test
    fun recordException_delegatesToNativeSdk() {
        val exception = IllegalStateException("boom")

        sut().recordException(exception)

        verify(exactly = 1) { nativeCrashlytics.recordException(exception) }
    }

    @Test
    fun recordExceptionWithKeys_translatesKeysToNativeContainer() {
        val exception = IllegalStateException("boom")
        val keysSlot = slot<CustomKeysAndValues>()
        every { nativeCrashlytics.recordException(exception, capture(keysSlot)) } just runs

        sut().recordException(
            throwable = exception,
            keys = CrashlyticsKeys().apply { put("fatal", false) }
        )

        verify(exactly = 1) { nativeCrashlytics.recordException(exception, any<CustomKeysAndValues>()) }
        val keyValues = keysSlot.captured.toKeyValuesMap()
        assertFalse(keyValues.containsValue("true"))
        assertTrue(keyValues.containsValue("false"))
    }

    @Test
    fun sendAndDeleteUnsentReports_delegateToNativeSdk() {
        val crashlytics = sut()

        crashlytics.sendUnsentReports()
        crashlytics.deleteUnsentReports()

        verify(exactly = 1) { nativeCrashlytics.sendUnsentReports() }
        verify(exactly = 1) { nativeCrashlytics.deleteUnsentReports() }
    }

    private fun CustomKeysAndValues.toKeyValuesMap(): Map<String, String> {
        val field = javaClass.getDeclaredField("keysAndValues")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        return field.get(this) as Map<String, String>
    }
}
