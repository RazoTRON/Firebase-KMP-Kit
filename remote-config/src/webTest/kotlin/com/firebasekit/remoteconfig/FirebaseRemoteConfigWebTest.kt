package com.firebasekit.remoteconfig

import com.firebasekit.remoteconfig.bridge.RemoteConfig
import com.firebasekit.remoteconfig.bridge.jsValueAsBoolean
import com.firebasekit.remoteconfig.bridge.jsValueAsFlattenJsonString
import com.firebasekit.remoteconfig.bridge.jsValueAsNumber
import com.firebasekit.remoteconfig.bridge.jsValueAsString
import kotlinx.coroutines.test.runTest
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalWasmJsInterop::class)
class FirebaseRemoteConfigWebTest {

    // ── Fake bridge ───────────────────────────────────────────────────────────

    private class FakeRemoteConfigBridge(
        private val shouldThrow: Boolean = false,
        private val valueProvider: (key: String) -> JsAny? = { null },
        private val allProvider: () -> JsAny? = { null },
    ) : RemoteConfig {
        var fetchCallCount = 0
            private set

        override suspend fun fetchAndActivate() {
            fetchCallCount++
            if (shouldThrow) throw Exception("fetch failed")
        }

        override fun getValue(key: String): JsAny? = valueProvider(key)
        override fun getAll(): JsAny? = allProvider()
    }

    private fun sut(bridge: FakeRemoteConfigBridge = FakeRemoteConfigBridge()) =
        FirebaseRemoteConfigWeb(bridge)

    // ── fetchAndActivate ───────────────────────────────────────────────────────

    @Test
    fun fetchAndActivate_delegatesToBridge() = runTest {
        val bridge = FakeRemoteConfigBridge()
        sut(bridge).fetchAndActivate()
        assertEquals(1, bridge.fetchCallCount)
    }

    @Test
    fun fetchAndActivate_throws_whenBridgeThrows() = runTest {
        val bridge = FakeRemoteConfigBridge(shouldThrow = true)
        val ex = assertFailsWith<Exception> { sut(bridge).fetchAndActivate() }
        assertEquals("fetch failed", ex.message)
    }

    // ── getBoolean ─────────────────────────────────────────────────────────────

    @Test
    fun getBoolean_returnsTrue_forJsValueWithTrueBoolean() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(bool = true) })
        assertEquals(true, sut(bridge).getBoolean("flag"))
    }

    @Test
    fun getBoolean_returnsFalse_forNullValue() {
        assertEquals(false, sut().getBoolean("missing"))
    }

    // ── getString ──────────────────────────────────────────────────────────────

    @Test
    fun getString_returnsString_forJsValue() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(string = "hello") })
        assertEquals("hello", sut(bridge).getString("greeting"))
    }

    @Test
    fun getString_returnsEmptyString_forNullValue() {
        assertEquals("", sut().getString("missing"))
    }

    // ── getDouble ──────────────────────────────────────────────────────────────

    @Test
    fun getDouble_returnsFiniteNumber_forJsValue() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = 3.14) })
        assertEquals(3.14, sut(bridge).getDouble("rate"))
    }

    @Test
    fun getDouble_returnsNull_whenNumberIsNaN() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = Double.NaN) })
        assertNull(sut(bridge).getDouble("rate"))
    }

    @Test
    fun getDouble_returnsNull_whenNumberIsInfinity() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = Double.POSITIVE_INFINITY) })
        assertNull(sut(bridge).getDouble("rate"))
    }

    @Test
    fun getDouble_returnsNull_forNullValue() {
        assertNull(sut().getDouble("missing"))
    }

    // ── getLong ────────────────────────────────────────────────────────────────

    @Test
    fun getLong_returnsLong_fromFiniteDouble() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = 42.9) })
        assertEquals(42L, sut(bridge).getLong("count"))
    }

    @Test
    fun getLong_returnsNull_forNaNDouble() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = Double.NaN) })
        assertNull(sut(bridge).getLong("count"))
    }

    // ── getInt ─────────────────────────────────────────────────────────────────

    @Test
    fun getInt_returnsInt_fromFiniteDouble() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = 7.0) })
        assertEquals(7, sut(bridge).getInt("num"))
    }

    @Test
    fun getInt_returnsNull_forNaNDouble() {
        val bridge = FakeRemoteConfigBridge(valueProvider = { makeJsValue(number = Double.NaN) })
        assertNull(sut(bridge).getInt("num"))
    }

    // ── allToJson ─────────────────────────────────────────────────────────────

    @Test
    fun allToJson_returnsEmptyString_forNullAllResult() {
        assertEquals("", sut().allToJson())
    }

    @Test
    fun allToJson_returnsJson_containingKeyAndValue() {
        val bridge = FakeRemoteConfigBridge(allProvider = { makeJsConfigMap("theme", "dark") })
        val json = sut(bridge).allToJson()
        assertTrue(json.contains("\"theme\""))
        assertTrue(json.contains("\"dark\""))
    }

    @Test
    fun allToJson_flattensMultipleEntries() {
        val bridge = FakeRemoteConfigBridge(allProvider = { makeJsConfigMap("k1", "v1", "k2", "v2") })
        val json = sut(bridge).allToJson()
        assertTrue(json.contains("\"k1\""))
        assertTrue(json.contains("\"v1\""))
        assertTrue(json.contains("\"k2\""))
        assertTrue(json.contains("\"v2\""))
    }

    // ── jsValueAsBoolean ──────────────────────────────────────────────────────

    @Test
    fun jsValueAsBoolean_returnsFalse_forNull() {
        assertEquals(false, jsValueAsBoolean(null))
    }

    @Test
    fun jsValueAsBoolean_returnsTrue_whenAsBooleanReturnsTrue() {
        assertEquals(true, jsValueAsBoolean(makeJsValue(bool = true)))
    }

    @Test
    fun jsValueAsBoolean_returnsFalse_whenObjectHasNoAsBooleanFunction() {
        assertEquals(false, jsValueAsBoolean(makeEmptyJsObject()))
    }

    // ── jsValueAsString ────────────────────────────────────────────────────────

    @Test
    fun jsValueAsString_returnsEmptyString_forNull() {
        assertEquals("", jsValueAsString(null))
    }

    @Test
    fun jsValueAsString_returnsValue_whenAsStringFunctionExists() {
        assertEquals("hello", jsValueAsString(makeJsValue(string = "hello")))
    }

    @Test
    fun jsValueAsString_returnsEmptyString_whenObjectHasNoAsStringFunction() {
        assertEquals("", jsValueAsString(makeEmptyJsObject()))
    }

    // ── jsValueAsNumber ────────────────────────────────────────────────────────

    @Test
    fun jsValueAsNumber_returnsNaN_forNull() {
        assertTrue(jsValueAsNumber(null).isNaN())
    }

    @Test
    fun jsValueAsNumber_returnsNumber_whenAsNumberFunctionExists() {
        assertEquals(99.5, jsValueAsNumber(makeJsValue(number = 99.5)))
    }

    @Test
    fun jsValueAsNumber_returnsNaN_whenObjectHasNoAsNumberFunction() {
        assertTrue(jsValueAsNumber(makeEmptyJsObject()).isNaN())
    }

    // ── jsValueAsFlattenJsonString ─────────────────────────────────────────────

    @Test
    fun jsValueAsFlattenJsonString_returnsEmptyString_forNull() {
        assertEquals("", jsValueAsFlattenJsonString(null))
    }

    @Test
    fun jsValueAsFlattenJsonString_returnsEmptyObject_forEmptyJsObject() {
        assertEquals("{}", jsValueAsFlattenJsonString(makeEmptyJsObject()))
    }

    @Test
    fun jsValueAsFlattenJsonString_flattensValuesByCallingAsString() {
        val json = jsValueAsFlattenJsonString(makeJsConfigMap("mode", "dark"))
        assertTrue(json.contains("\"mode\""))
        assertTrue(json.contains("\"dark\""))
    }

    @Test
    fun jsValueAsFlattenJsonString_usesStringCoercion_forValuesWithoutAsString() {
        val json = jsValueAsFlattenJsonString(makeJsPlainMap("count", "42"))
        assertTrue(json.contains("\"count\""))
        assertTrue(json.contains("42"))
    }
}

// ── Top-level JS factory functions ────────────────────────────────────────────

@OptIn(ExperimentalWasmJsInterop::class)
private fun makeJsValue(bool: Boolean = false, string: String = "", number: Double = Double.NaN): JsAny =
    js("({ asBoolean: function() { return bool; }, asString: function() { return string; }, asNumber: function() { return number; } })")

@OptIn(ExperimentalWasmJsInterop::class)
private fun makeEmptyJsObject(): JsAny = js("({})")

@OptIn(ExperimentalWasmJsInterop::class)
private fun makeJsConfigMap(key: String, value: String): JsAny =
    js("({ [key]: { asString: function() { return value; } } })")

@OptIn(ExperimentalWasmJsInterop::class)
private fun makeJsConfigMap(k1: String, v1: String, k2: String, v2: String): JsAny =
    js("({ [k1]: { asString: function() { return v1; } }, [k2]: { asString: function() { return v2; } } })")

@OptIn(ExperimentalWasmJsInterop::class)
private fun makeJsPlainMap(key: String, value: String): JsAny =
    js("({ [key]: value })")
