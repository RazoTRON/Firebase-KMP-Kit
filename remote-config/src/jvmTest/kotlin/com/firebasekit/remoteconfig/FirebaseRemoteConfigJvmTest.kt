package com.firebasekit.remoteconfig

import com.firebasekit.core.FirebaseJvm
import com.firebasekit.remoteconfig.models.InstallationResponse
import com.firebasekit.remoteconfig.models.RemoteConfigResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Unit tests for [FirebaseRemoteConfigJvm].
 *
 * Since [FirebaseRemoteConfigJvm] now accepts [HttpClient] via its constructor, every test
 * creates a fresh instance with a [MockEngine] — no reflection required for the client itself.
 * [FirebaseJvm] credentials (private-set properties) are still written via reflection because
 * [FirebaseJvm.initialize] is `internal` to the :core module.
 */
class FirebaseRemoteConfigJvmTest {

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private val json = Json { ignoreUnknownKeys = true }

    private val defaultInstallation = InstallationResponse(
        fid = "test-fid-001",
        refreshToken = "test-refresh-token",
        authToken = InstallationResponse.AuthToken(
            token = "test-auth-token",
            expiresIn = "604800s",
        ),
    )

    // ── Setup / teardown ──────────────────────────────────────────────────────

    @BeforeTest
    fun setup() {
        setFirebaseJvmField("apiKey", "test-api-key")
        setFirebaseJvmField("projectId", "test-project-id")
        setFirebaseJvmField("appId", "1:000:android:abc123")
        setFirebaseJvmField("fid", "test-fid-001")
    }

    @AfterTest
    fun teardown() {
        setFirebaseJvmField("apiKey", null)
        setFirebaseJvmField("projectId", null)
        setFirebaseJvmField("appId", null)
        setFirebaseJvmField("fid", null)
    }

    // ── fetchAndActivate ──────────────────────────────────────────────────────

    @Test
    fun fetchAndActivate_populatesConfigValues_fromRemoteResponse() = runTest {
        val remoteEntries = mapOf(
            "dark_mode" to "true",
            "welcome_msg" to "Hello",
            "max_retries" to "3",
            "threshold" to "0.85",
        )
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse(entries = remoteEntries)),
        )

        sut.fetchAndActivate()

        assertEquals("Hello", sut.getString("welcome_msg"))
        assertEquals(true, sut.getBoolean("dark_mode"))
        assertEquals(3L, sut.getLong("max_retries"))
        assertEquals(0.85, sut.getDouble("threshold"))
        assertEquals(3, sut.getInt("max_retries"))
    }

    @Test
    fun fetchAndActivate_handlesEmptyEntries_returnsNullForAnyKey() = runTest {
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse(entries = emptyMap())),
        )

        sut.fetchAndActivate()

        assertNull(sut.getString("any_key"))
        assertNull(sut.getBoolean("any_key"))
        assertNull(sut.getLong("any_key"))
    }

    @Test
    fun fetchAndActivate_reusesInstallation_onSubsequentCalls() = runTest {
        var installationCallCount = 0
        val engine = MockEngine { request ->
            when {
                "firebaseinstallations" in request.url.host -> {
                    installationCallCount++
                    respond(
                        content = json.encodeToString(defaultInstallation),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                "firebaseremoteconfig" in request.url.host -> respond(
                    content = json.encodeToString(RemoteConfigResponse()),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> error("Unexpected request to: ${request.url}")
            }
        }
        val sut = FirebaseRemoteConfigJvm(buildClient(engine))

        sut.fetchAndActivate()
        sut.fetchAndActivate()

        // Installation is created once and cached inside the instance
        assertEquals(1, installationCallCount)
    }

    @Test
    fun fetchAndActivate_throws_whenApiKeyIsNotSet() = runTest {
        setFirebaseJvmField("apiKey", null)
        val sut = buildSut(
            installationJson = "",
            remoteConfigJson = "",
        )

        assertFailsWith<Exception> { sut.fetchAndActivate() }
    }

    @Test
    fun fetchAndActivate_throws_whenProjectIdIsNotSet() = runTest {
        setFirebaseJvmField("projectId", null)
        val sut = buildSut(
            installationJson = "",
            remoteConfigJson = "",
        )

        assertFailsWith<Exception> { sut.fetchAndActivate() }
    }

    @Test
    fun fetchAndActivate_throws_whenAppIdIsNotSet() = runTest {
        setFirebaseJvmField("appId", null)
        val sut = buildSut(
            installationJson = "",
            remoteConfigJson = "",
        )

        assertFailsWith<Exception> { sut.fetchAndActivate() }
    }

    @Test
    fun fetchAndActivate_throws_whenFidIsNotSet() = runTest {
        setFirebaseJvmField("fid", null)
        val sut = buildSut(
            installationJson = "",
            remoteConfigJson = "",
        )

        assertFailsWith<Exception> { sut.fetchAndActivate() }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    @Test
    fun getString_returnsNull_forMissingKey() = runTest {
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse(entries = mapOf("other" to "x"))),
        )

        sut.fetchAndActivate()

        assertNull(sut.getString("missing_key"))
    }

    @Test
    fun getBoolean_returnsNull_forMissingKey() = runTest {
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse()),
        )

        sut.fetchAndActivate()

        assertNull(sut.getBoolean("missing_key"))
    }

    @Test
    fun getDouble_returnsNull_forMissingKey() = runTest {
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse()),
        )

        sut.fetchAndActivate()

        assertNull(sut.getDouble("missing_key"))
    }

    // ── allToJson ─────────────────────────────────────────────────────────────

    @Test
    fun allToJson_returnsEmptyObject_beforeFetch() {
        val sut = FirebaseRemoteConfigJvm(buildClient(emptyMockEngine()))

        assertEquals("{}", sut.allToJson())
    }

    @Test
    fun allToJson_containsAllFetchedEntries() = runTest {
        val expected = mapOf("alpha" to "1", "beta" to "hello", "gamma" to "true")
        val sut = buildSut(
            installationJson = json.encodeToString(defaultInstallation),
            remoteConfigJson = json.encodeToString(RemoteConfigResponse(entries = expected)),
        )

        sut.fetchAndActivate()

        val parsed = json.decodeFromString<Map<String, String>>(sut.allToJson())
        assertEquals(expected, parsed)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates a fresh [FirebaseRemoteConfigJvm] backed by a [MockEngine] routing by host. */
    private fun buildSut(installationJson: String, remoteConfigJson: String): FirebaseRemoteConfigJvm =
        FirebaseRemoteConfigJvm(buildClient(routingEngine(installationJson, remoteConfigJson)))

    private fun routingEngine(installationJson: String, remoteConfigJson: String) =
        MockEngine { request ->
            when {
                "firebaseinstallations" in request.url.host -> respond(
                    content = installationJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                "firebaseremoteconfig" in request.url.host -> respond(
                    content = remoteConfigJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> error("Unexpected request to: ${request.url}")
            }
        }

    private fun emptyMockEngine() = MockEngine { error("No requests expected") }

    private fun buildClient(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /** Sets a [FirebaseJvm] property that has a private setter, via reflection. */
    private fun setFirebaseJvmField(name: String, value: Any?) {
        val field = FirebaseJvm::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(FirebaseJvm, value)
    }
}
