package com.firebasekit.analytics

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ANALYTICS_ENDPOINT = "https://www.google-analytics.com/mp/collect"

actual val Firebase.analytics: FirebaseAnalytics by lazy {
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

    FirebaseAnalyticsJvm(client)
}

class FirebaseAnalyticsJvm(
    private val client: HttpClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : FirebaseAnalytics {
    private var collectionEnabled = true
    private var userId: String? = null
    private val userProperties = linkedMapOf<String, String>()

    override fun logEvent(name: String, parameters: Bundle) {
        if (collectionEnabled.not()) return

        scope.launch {
            runCatching { sendEvent(name, parameters) }
        }
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        collectionEnabled = enabled
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setUserProperty(name: String, value: String) {
        userProperties[name] = value
    }

    override fun resetAnalyticsData() {
        userId = null
        userProperties.clear()
    }

    private suspend fun sendEvent(name: String, parameters: Bundle) {
        val measurementId = FirebaseJvm.measurementId ?: throw Exception("Firebase app is not initialized")
        val apiSecret = FirebaseJvm.analyticsApiSecret ?: throw Exception("Analytics API secret is not set")
        val clientId = FirebaseJvm.clientId ?: throw Exception("Analytics client ID is not set")

        client.post(ANALYTICS_ENDPOINT) {
            parameter("measurement_id", measurementId)
            parameter("api_secret", apiSecret)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    userId?.let { put("user_id", it) }
                    put("client_id", clientId)
                    putUserProperties()

                    put(
                        "events",
                        buildEvent(
                            name = name,
                            parameters = parameters,
                            clientId = clientId
                        )
                    )
                }
            )
        }
    }

    private fun buildEvent(
        name: String,
        parameters: Bundle,
        clientId: String
    ): JsonArray = buildJsonArray {
        add(
            buildJsonObject {
                put("name", name)
                val eventParams = parameters.toJsonObject(clientId)
                if (eventParams.isNotEmpty()) {
                    put("params", eventParams)
                }
            }
        )
    }

    private fun JsonObjectBuilder.putUserProperties() {
        if (userProperties.isEmpty()) return

        put(
            "user_properties",
            buildJsonObject {
                userProperties.forEach { (name, value) ->
                    put(name, buildJsonObject { put("value", value) })
                }
            }
        )
    }

    private fun Bundle.toJsonObject(clientId: String): JsonObject = buildJsonObject {
        values.forEach { (key, value) -> put(key, value.toJsonElement()) }
        put("session_id", clientId)
    }

    private fun BundleValue.toJsonElement(): JsonElement = when (this) {
        is BundleValue.StringValue -> JsonPrimitive(value)
        is BundleValue.IntValue -> JsonPrimitive(value)
        is BundleValue.FloatValue -> JsonPrimitive(value)
        is BundleValue.LongValue -> JsonPrimitive(value)
        is BundleValue.DoubleValue -> JsonPrimitive(value)
        is BundleValue.BooleanValue -> JsonPrimitive(if (value) 1L else 0L)
        is BundleValue.SerializableValue -> JsonPrimitive(Json.encodeToString(value))
    }
}
