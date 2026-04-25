package com.firebasekit.core

import com.firebasekit.core.models.InstallationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.time.Clock

internal class FirebaseCache(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val client by lazy {
        HttpClient(Java) {
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
    }

    lateinit var file: File
    private var model = CompletableDeferred<Model>()
    private val json = Json { ignoreUnknownKeys = true }

    fun init(cacheFilePath: String) {
        file = File(cacheFilePath)
        createIfNotExist()
    }

    suspend fun refreshCachedData() {
        model = CompletableDeferred()
        runCatching { createCachedData() }
            .onSuccess { model.complete(it) }
            .onFailure { model.completeExceptionally(it) }
    }

    private fun createIfNotExist() {
        scope.launch {
            runCatching { createOrReadModel() }
                .onSuccess { model.complete(it) }
                .onFailure { model.completeExceptionally(it) }
        }
    }

    suspend fun getFID(): String = model.await().fid

    suspend fun getClientId(): String = model.await().clientId

    private suspend fun createOrReadModel(): Model = withContext(Dispatchers.IO) {
        if (file.exists()) {
            val cached = file.readText()
            runCatching { json.decodeFromString<Model>(cached) }
                .getOrElse { createCachedData() }
        } else {
            createCachedData()
        }
    }

    private suspend fun createCachedData(): Model {
        val installation = createInstallation()

        return Model(
            clientId = generateClientId(),
            fid = installation.fid,
        ).also { model ->
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(model))
        }
    }

    private fun generateClientId(): String {
        return "${Random.Default.nextLong(1000000000, 9999999999)}.${Clock.System.now().epochSeconds}"
    }

    suspend fun createInstallation(): InstallationResponse {
        val apiKey = FirebaseJvm.apiKey ?: throw Exception("Firebase app is not initialized")
        val projectId = FirebaseJvm.projectId ?: throw Exception("Firebase project ID is not set")
        val appId = FirebaseJvm.appId ?: throw Exception("Firebase app ID is not set")

        val cachedData = runCatching { json.decodeFromString<Model>(file.readText()) }.getOrNull()

        val response = client.post(
            "https://firebaseinstallations.googleapis.com/v1/projects/$projectId/installations"
        ) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", apiKey)
            setBody(
                mapOf(
                    "fid" to cachedData?.fid,
                    "appId" to appId,
                    "authVersion" to "FIS_v2",
                    "sdkVersion" to "w:0.6.18",
                )
            )
        }.body<InstallationResponse>()

        return response
    }

    @Serializable
    data class Model(
        val clientId: String,
        val fid: String,
    )
}