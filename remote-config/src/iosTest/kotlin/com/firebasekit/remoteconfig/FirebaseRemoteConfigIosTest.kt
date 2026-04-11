package com.firebasekit.remoteconfig

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDate
import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigFetchAndActivateStatus
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigFetchStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class FirebaseRemoteConfigIosTest {

    // ── Fakes ─────────────────────────────────────────────────────────────────

    private class FakeValue(
        override val boolValue: Boolean = false,
        override val stringValue: String = "",
        override val longValue: Long = 0L,
        override val intValue: Int = 0,
        override val doubleValue: Double = Double.NaN,
    ) : RemoteConfig.Value

    private class FakeSettings(
        override val minimumFetchInterval: Double = 0.0,
    ) : RemoteConfig.Settings

    private class FakeRemoteConfig(
        private val statusToReturn: FIRRemoteConfigFetchAndActivateStatus =
            FIRRemoteConfigFetchAndActivateStatus.FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote,
        private val errorToReturn: NSError? = null,
        private val values: Map<String, RemoteConfig.Value> = emptyMap(),
        private val keys: Set<String> = emptySet(),
        override val lastFetchStatus: FIRRemoteConfigFetchStatus =
            FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusNoFetchYet,
        override val lastFetchTime: NSDate? = null,
        override val configSettings: RemoteConfig.Settings = FakeSettings(),
    ) : RemoteConfig {

        override fun keysWithPrefix(prefix: String): Set<String> = keys

        override fun configValueForKey(key: String): RemoteConfig.Value =
            values[key] ?: FakeValue()

        override fun fetchAndActivateWithCompletionHandler(
            completionHandler: ((FIRRemoteConfigFetchAndActivateStatus, NSError?) -> Unit)?
        ) = completionHandler?.invoke(statusToReturn, errorToReturn) ?: Unit
    }

    private fun sut(config: FakeRemoteConfig = FakeRemoteConfig()) =
        FirebaseRemoteConfigIos(config)

    // ── fetchAndActivate ───────────────────────────────────────────────────────

    @Test
    fun fetchAndActivate_completesSuccessfully_whenStatusIsFetchedFromRemote() = runTest {
        sut().fetchAndActivate()
    }

    @Test
    fun fetchAndActivate_throws_whenStatusIsUsingPreFetchedData() = runTest {
        val config = FakeRemoteConfig(
            statusToReturn = FIRRemoteConfigFetchAndActivateStatus
                .FIRRemoteConfigFetchAndActivateStatusSuccessUsingPreFetchedData,
        )
        val ex = assertFailsWith<Exception> { sut(config).fetchAndActivate() }
        assertEquals("fetchAndActivate failed", ex.message)
    }

    @Test
    fun fetchAndActivate_throws_withErrorMessage_whenStatusIsError() = runTest {
        val error = NSError.errorWithDomain("FirebaseRemoteConfig", 1L, null)
        val config = FakeRemoteConfig(
            statusToReturn = FIRRemoteConfigFetchAndActivateStatus
                .FIRRemoteConfigFetchAndActivateStatusError,
            errorToReturn = error,
        )
        val ex = assertFailsWith<Exception> { sut(config).fetchAndActivate() }
        assertNotNull(ex.message)
        assertTrue(ex.message!!.isNotEmpty())
    }

    @Test
    fun fetchAndActivate_throws_withFallbackMessage_whenStatusIsErrorAndNoNSError() = runTest {
        val config = FakeRemoteConfig(
            statusToReturn = FIRRemoteConfigFetchAndActivateStatus
                .FIRRemoteConfigFetchAndActivateStatusError,
            errorToReturn = null,
        )
        val ex = assertFailsWith<Exception> { sut(config).fetchAndActivate() }
        assertEquals("fetchAndActivate failed", ex.message)
    }

    // ── getBoolean ─────────────────────────────────────────────────────────────

    @Test
    fun getBoolean_returnsTrue_forKeyWithTrueValue() {
        val config = FakeRemoteConfig(values = mapOf("flag" to FakeValue(boolValue = true)))
        assertEquals(true, sut(config).getBoolean("flag"))
    }

    @Test
    fun getBoolean_returnsFalse_forMissingKey() {
        assertEquals(false, sut().getBoolean("missing"))
    }

    // ── getString ──────────────────────────────────────────────────────────────

    @Test
    fun getString_returnsValue_forConfiguredKey() {
        val config = FakeRemoteConfig(values = mapOf("greeting" to FakeValue(stringValue = "hello")))
        assertEquals("hello", sut(config).getString("greeting"))
    }

    @Test
    fun getString_returnsEmptyString_forMissingKey() {
        assertEquals("", sut().getString("missing"))
    }

    // ── getLong ────────────────────────────────────────────────────────────────

    @Test
    fun getLong_returnsValue_forConfiguredKey() {
        val config = FakeRemoteConfig(values = mapOf("count" to FakeValue(longValue = 99L)))
        assertEquals(99L, sut(config).getLong("count"))
    }

    @Test
    fun getLong_returnsZero_forMissingKey() {
        assertEquals(0L, sut().getLong("missing"))
    }

    // ── getInt ─────────────────────────────────────────────────────────────────

    @Test
    fun getInt_returnsValue_forConfiguredKey() {
        val config = FakeRemoteConfig(values = mapOf("limit" to FakeValue(intValue = 10)))
        assertEquals(10, sut(config).getInt("limit"))
    }

    @Test
    fun getInt_returnsZero_forMissingKey() {
        assertEquals(0, sut().getInt("missing"))
    }

    // ── getDouble ──────────────────────────────────────────────────────────────

    @Test
    fun getDouble_returnsFiniteValue_forConfiguredKey() {
        val config = FakeRemoteConfig(values = mapOf("rate" to FakeValue(doubleValue = 3.14)))
        assertEquals(3.14, sut(config).getDouble("rate"))
    }

    @Test
    fun getDouble_returnsNull_whenValueIsNaN() {
        val config = FakeRemoteConfig(values = mapOf("bad" to FakeValue(doubleValue = Double.NaN)))
        assertNull(sut(config).getDouble("bad"))
    }

    @Test
    fun getDouble_returnsNull_whenValueIsPositiveInfinity() {
        val config = FakeRemoteConfig(
            values = mapOf("inf" to FakeValue(doubleValue = Double.POSITIVE_INFINITY))
        )
        assertNull(sut(config).getDouble("inf"))
    }

    @Test
    fun getDouble_returnsNull_whenValueIsNegativeInfinity() {
        val config = FakeRemoteConfig(
            values = mapOf("ninf" to FakeValue(doubleValue = Double.NEGATIVE_INFINITY))
        )
        assertNull(sut(config).getDouble("ninf"))
    }

    @Test
    fun getDouble_returnsNull_forMissingKey() {
        // Default FakeValue has doubleValue = Double.NaN
        assertNull(sut().getDouble("missing"))
    }

    // ── allToJson ─────────────────────────────────────────────────────────────

    @Test
    fun allToJson_returnsEmptyObject_whenNoKeys() {
        assertEquals("{}", sut().allToJson())
    }

    @Test
    fun allToJson_returnsJson_containingKeyAndValue() {
        val config = FakeRemoteConfig(
            keys = setOf("theme"),
            values = mapOf("theme" to FakeValue(stringValue = "dark")),
        )
        val json = sut(config).allToJson()
        assertNotNull(json)
        assertTrue(json.contains("theme"))
        assertTrue(json.contains("dark"))
    }

    @Test
    fun allToJson_returnsJson_forMultipleKeys() {
        val config = FakeRemoteConfig(
            keys = setOf("k1", "k2"),
            values = mapOf(
                "k1" to FakeValue(stringValue = "v1"),
                "k2" to FakeValue(stringValue = "v2"),
            ),
        )
        val json = sut(config).allToJson()
        assertNotNull(json)
        assertTrue(json.contains("k1"))
        assertTrue(json.contains("k2"))
    }

    // ── getLastFetchTime ──────────────────────────────────────────────────────

    @Test
    fun getLastFetchTime_returnsNull_whenLastFetchTimeIsNull() {
        assertNull(sut().getLastFetchTime())
    }

    @Test
    fun getLastFetchTime_returnsInstant_whenLastFetchTimeIsSet() {
        val config = FakeRemoteConfig(lastFetchTime = NSDate())
        assertNotNull(sut(config).getLastFetchTime())
    }

    // ── getLastFetchStatus ────────────────────────────────────────────────────

    @Test
    fun getLastFetchStatus_returnsSuccess_forSuccessStatus() {
        val config = FakeRemoteConfig(
            lastFetchStatus = FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusSuccess
        )
        assertEquals("SUCCESS", sut(config).getLastFetchStatus())
    }

    @Test
    fun getLastFetchStatus_returnsFailure_forFailureStatus() {
        val config = FakeRemoteConfig(
            lastFetchStatus = FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusFailure
        )
        assertEquals("FAILURE", sut(config).getLastFetchStatus())
    }

    @Test
    fun getLastFetchStatus_returnsThrottled_forThrottledStatus() {
        val config = FakeRemoteConfig(
            lastFetchStatus = FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusThrottled
        )
        assertEquals("THROTTLED", sut(config).getLastFetchStatus())
    }

    @Test
    fun getLastFetchStatus_returnsUnknown_forNoFetchYetStatus() {
        val config = FakeRemoteConfig(
            lastFetchStatus = FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusNoFetchYet
        )
        assertEquals("UNKNOWN", sut(config).getLastFetchStatus())
    }

    // ── getMinimumFetchInterval ───────────────────────────────────────────────

    @Test
    fun getMinimumFetchInterval_returnsIntervalInWholeSeconds() {
        val config = FakeRemoteConfig(configSettings = FakeSettings(minimumFetchInterval = 3600.0))
        assertEquals(3600L, sut(config).getMinimumFetchInterval())
    }

    @Test
    fun getMinimumFetchInterval_returnsZero_forZeroInterval() {
        val config = FakeRemoteConfig(configSettings = FakeSettings(minimumFetchInterval = 0.0))
        assertEquals(0L, sut(config).getMinimumFetchInterval())
    }
}
