package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm
import com.firebasekit.remoteconfig.models.InstallationResponse
import com.firebasekit.remoteconfig.models.RemoteConfigRequestBody
import com.firebasekit.remoteconfig.models.RemoteConfigResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() {
        val client = HttpClient(Java) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        return FirebaseRemoteConfigJvm(client)
    }

class FirebaseRemoteConfigJvm(private val client: HttpClient) : FirebaseRemoteConfig {
    private val configValues = mutableMapOf<String, String>()
    private val defaultRefreshInterval = 60.minutes

    private var installation: InstallationResponse? = null

    override suspend fun fetchAndActivate() {
        val updateInterval = FirebaseJvm.interval ?: defaultRefreshInterval
        updateRemoteConfigs()

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(updateInterval)
                updateRemoteConfigs()
            }
        }
    }

    private suspend fun updateRemoteConfigs() {
        val apiKey = FirebaseJvm.apiKey ?: throw Exception("Firebase app is not initialized")
        val projectId = FirebaseJvm.projectId ?: throw Exception("Firebase project ID is not set")
        val appId = FirebaseJvm.appId ?: throw Exception("Firebase app ID is not set")
        val fid = FirebaseJvm.fid ?: throw Exception("Firebase FID is not created")

        val currentInstallation = installation ?: createInstallation(
            fid = fid,
            apiKey = apiKey,
            projectId = projectId,
            appId = appId
        ).also { installation = it }

        val response = fetchRemoteConfig(apiKey, projectId, appId, currentInstallation)
        configValues.clear()
        configValues.putAll(response.entries)
    }

    private suspend fun createInstallation(
        fid: String,
        apiKey: String,
        projectId: String,
        appId: String,
    ): InstallationResponse {
        val response = client.post(
            "https://firebaseinstallations.googleapis.com/v1/projects/$projectId/installations"
        ) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", apiKey)
            setBody(
                mapOf(
                    "fid" to fid,
                    "appId" to appId,
                    "authVersion" to "FIS_v2",
                    "sdkVersion" to "w:0.6.18",
                )
            )
        }

        return response.body()
    }

    private suspend fun fetchRemoteConfig(
        apiKey: String,
        projectId: String,
        appId: String,
        installation: InstallationResponse,
    ): RemoteConfigResponse {
        return client.post(
            "https://firebaseremoteconfig.googleapis.com/v1/projects/$projectId/namespaces/firebase:fetch"
        ) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                RemoteConfigRequestBody(
                    appId = appId,
                    appInstanceId = installation.fid,
                    appInstanceIdToken = installation.authToken.token
                )
            )
        }.body()
    }

    override fun getString(key: String): String? = configValues[key]
    override fun getBoolean(key: String): Boolean? = configValues[key]?.toBooleanStrict()
    override fun getDouble(key: String): Double? = configValues[key]?.toDouble()
    override fun getLong(key: String): Long? = configValues[key]?.toLong()
    override fun getInt(key: String): Int? = getLong(key)?.toInt()
    override fun allToJson(): String = Json.encodeToString(configValues)
}
