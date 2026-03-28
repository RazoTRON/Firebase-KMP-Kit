package com.firebasekit.remoteconfig

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig as AndroidRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class FirebaseRemoteConfigAndroidTest {

    // ── Fakes / helpers ───────────────────────────────────────────────────────

    private val mockInstance: AndroidRemoteConfig = mockk()

    private fun sut() = FirebaseRemoteConfigAndroid(remoteConfig = mockInstance)

    /** Returns a Task that immediately invokes its OnCompleteListener. */
    private fun successTask(): Task<Boolean> = mockk<Task<Boolean>>().also { task ->
        every { task.isSuccessful } returns true
        every { task.result } returns true
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Boolean>>().onComplete(task)
            task
        }
    }

    private fun failureTask(cause: Exception): Task<Boolean> = mockk<Task<Boolean>>().also { task ->
        every { task.isSuccessful } returns false
        every { task.exception } returns cause
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Boolean>>().onComplete(task)
            task
        }
    }

    private fun failureTaskNoException(): Task<Boolean> = mockk<Task<Boolean>>().also { task ->
        every { task.isSuccessful } returns false
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Boolean>>().onComplete(task)
            task
        }
    }

    private fun mockValue(
        string: String = "",
        boolean: Boolean = false,
        double: Double = 0.0,
        long: Long = 0L,
    ): FirebaseRemoteConfigValue = mockk<FirebaseRemoteConfigValue>().also { v ->
        every { v.asString() } returns string
        every { v.asBoolean() } returns boolean
        every { v.asDouble() } returns double
        every { v.asLong() } returns long
    }

    // ── fetchAndActivate ───────────────────────────────────────────────────────

    @Test
    fun fetchAndActivate_completesSuccessfully_whenTaskSucceeds() = runTest {
        every { mockInstance.fetchAndActivate() } returns successTask()
        sut().fetchAndActivate()
        verify(exactly = 1) { mockInstance.fetchAndActivate() }
    }

    @Test
    fun fetchAndActivate_throws_whenTaskFails() = runTest {
        val cause = RuntimeException("network error")
        every { mockInstance.fetchAndActivate() } returns failureTask(cause)
        val ex = assertFailsWith<RuntimeException> { sut().fetchAndActivate() }
        assertEquals("network error", ex.message)
    }

    @Test
    fun fetchAndActivate_throwsWithFallbackMessage_whenTaskFailsWithNoException() = runTest {
        every { mockInstance.fetchAndActivate() } returns failureTaskNoException()
        val ex = assertFailsWith<Exception> { sut().fetchAndActivate() }
        assertNotNull(ex.message)
    }

    // ── getBoolean ─────────────────────────────────────────────────────────────

    @Test
    fun getBoolean_returnsTrue_forKeyWithTrueValue() {
        every { mockInstance.getBoolean("flag") } returns true
        assertEquals(true, sut().getBoolean("flag"))
    }

    @Test
    fun getBoolean_returnsFalse_forKeyWithFalseValue() {
        every { mockInstance.getBoolean("flag") } returns false
        assertEquals(false, sut().getBoolean("flag"))
    }

    // ── getString ──────────────────────────────────────────────────────────────

    @Test
    fun getString_returnsValue_forConfiguredKey() {
        every { mockInstance.getString("greeting") } returns "hello"
        assertEquals("hello", sut().getString("greeting"))
    }

    @Test
    fun getString_returnsEmptyString_forMissingKey() {
        every { mockInstance.getString("missing") } returns ""
        assertEquals("", sut().getString("missing"))
    }

    // ── getDouble ──────────────────────────────────────────────────────────────

    @Test
    fun getDouble_returnsValue_forConfiguredKey() {
        every { mockInstance.getDouble("rate") } returns 3.14
        assertEquals(3.14, sut().getDouble("rate"))
    }

    // ── getLong ────────────────────────────────────────────────────────────────

    @Test
    fun getLong_returnsValue_forConfiguredKey() {
        every { mockInstance.getLong("count") } returns 42L
        assertEquals(42L, sut().getLong("count"))
    }

    // ── getInt ─────────────────────────────────────────────────────────────────

    @Test
    fun getInt_returnsLongCastToInt_forConfiguredKey() {
        every { mockInstance.getLong("limit") } returns 10L
        assertEquals(10, sut().getInt("limit"))
    }

    @Test
    fun getInt_truncatesLong_whenValueExceedsIntRange() {
        every { mockInstance.getLong("big") } returns (Int.MAX_VALUE.toLong() + 1L)
        assertEquals(Int.MIN_VALUE, sut().getInt("big"))
    }

    // ── allToJson ─────────────────────────────────────────────────────────────

    @Test
    fun allToJson_returnsEmptyObject_whenNoEntries() {
        every { mockInstance.all } returns emptyMap()
        assertEquals("{}", sut().allToJson())
    }

    @Test
    fun allToJson_returnsJsonWithAllEntries() {
        every { mockInstance.all } returns mapOf(
            "theme" to mockValue(string = "dark"),
            "retries" to mockValue(string = "3"),
        )
        val json = sut().allToJson()
        assert(json.contains("\"theme\""))
        assert(json.contains("\"dark\""))
        assert(json.contains("\"retries\""))
        assert(json.contains("\"3\""))
    }

    @Test
    fun allToJson_returnsValidJsonString_forSingleEntry() {
        every { mockInstance.all } returns mapOf("key" to mockValue(string = "value"))
        assertEquals("{\"key\":\"value\"}", sut().allToJson())
    }
}
