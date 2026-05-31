package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm
import com.firebasekit.remoteconfig.models.RemoteConfigRequestBody
import com.firebasekit.remoteconfig.models.RemoteConfigResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal val remoteConfigJvm: FirebaseRemoteConfigJvm by lazy {
    val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message + "\n")
                }
            }

            level = LogLevel.BODY
        }
    }

    FirebaseRemoteConfigJvm(client)
}

actual val Firebase.remoteConfig: FirebaseRemoteConfig by lazy { remoteConfigJvm }

class FirebaseRemoteConfigJvm(private val client: HttpClient) : JvmRemoteConfig() {
    private val configValues = mutableMapOf<String, String>()

    override suspend fun fetchAndActivate() {
        CoroutineScope(Dispatchers.IO).launch {
            settings.refreshInterval.collectLatest {
                while (true) {
                    updateRemoteConfigs(FirebaseJvm)
                    delay(it)
                }
            }
        }
    }

    private suspend fun updateRemoteConfigs(firebase: FirebaseJvm) {
        val response = fetchRemoteConfig(firebase)

        val remoteConfig: RemoteConfigResponse = if (response.status.isSuccess()) {
            response.body()
        } else {
            firebase.refreshCachedData()

            fetchRemoteConfig(firebase).body()
        }

        configValues.clear()
        configValues.putAll(remoteConfig.entries)
    }

    private suspend fun fetchRemoteConfig(firebase: FirebaseJvm): HttpResponse {
        val apiKey = firebase.apiKey ?: throw Exception("Firebase app is not initialized")
        val projectId = firebase.projectId ?: throw Exception("Firebase project ID is not set")
        val appId = firebase.appId ?: throw Exception("Firebase app ID is not set")
        val fid = firebase.fid ?: throw Exception("Firebase FID is not set")

        return client.post(
            "https://firebaseremoteconfig.googleapis.com/v1/projects/$projectId/namespaces/firebase:fetch"
        ) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                RemoteConfigRequestBody(
                    appId = appId,
                    appInstanceId = fid,
                )
            )
        }
    }

    override fun getString(key: String): String? = configValues[key]
    override fun getBoolean(key: String): Boolean? = configValues[key]?.toBooleanStrict()
    override fun getDouble(key: String): Double? = configValues[key]?.toDouble()
    override fun getLong(key: String): Long? = configValues[key]?.toLong()
    override fun getInt(key: String): Int? = getLong(key)?.toInt()
    override fun allToJson(): String = Json.encodeToString(configValues)
}