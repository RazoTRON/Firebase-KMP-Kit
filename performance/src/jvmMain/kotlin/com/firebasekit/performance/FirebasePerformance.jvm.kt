package com.firebasekit.performance

import com.firebasekit.core.Firebase
import com.firebasekit.core.FirebaseJvm
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val PERFORMANCE_ENDPOINT =
    "https://firebaselogging-pa.googleapis.com/v1/firelog/legacy/log?key=AIzaSyCx80ru6-RXeTi3GvqkFsMVyMf-vpgIoVw"
private const val PERFORMANCE_LOG_SOURCE = 462
private const val PERFORMANCE_CLIENT_TYPE_JS = 1
private const val PERFORMANCE_SDK_VERSION = "0.6.9"

actual val Firebase.performance: FirebasePerformance by lazy {
    FirebasePerformanceJvm(defaultHttpClient())
}

private fun defaultHttpClient(): HttpClient {
    return HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        if (Firebase.enableLogs) {
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
}

class FirebasePerformanceJvm(
    private val client: HttpClient = defaultHttpClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : FirebasePerformance {
    private val collectionEnabled = AtomicBoolean(true)

    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        collectionEnabled.set(enabled)
    }

    override fun newTrace(name: String): PerformanceTrace {
        return PerformanceTraceJvm(
            name = name,
            isCollectionEnabled = collectionEnabled::get,
            onStop = ::sendTraceInBackground,
        )
    }

    override fun newHttpMetric(url: String, httpMethod: String): PerformanceHttpMetric {
        return PerformanceHttpMetricJvm(
            url = url,
            httpMethod = httpMethod,
            isCollectionEnabled = collectionEnabled::get,
            onStop = ::sendHttpMetricInBackground,
        )
    }

    internal suspend fun sendTrace(trace: PerformanceTraceJvm): HttpResponse {
        return postEvent(
            event = trace.toFirelogEvent(),
            eventTimeMs = trace.getEventTimeMs(),
        )
    }

    internal suspend fun sendHttpMetric(metric: PerformanceHttpMetricJvm): HttpResponse {
        return postEvent(
            event = metric.toFirelogEvent(),
            eventTimeMs = metric.getEventTimeMs(),
        )
    }

    private fun sendTraceInBackground(trace: PerformanceTraceJvm) {
        scope.launch {
            runCatching { sendTrace(trace) }
        }
    }

    private fun sendHttpMetricInBackground(metric: PerformanceHttpMetricJvm) {
        scope.launch {
            runCatching { sendHttpMetric(metric) }
        }
    }

    private suspend fun postEvent(
        event: JsonObject,
        eventTimeMs: Long,
    ): HttpResponse {
        val response = postPayload(event, eventTimeMs)
        if (response.status.isSuccess()) return response

        FirebaseJvm.refreshCachedData()
        return postPayload(event, eventTimeMs)
    }

    private suspend fun postPayload(
        event: JsonObject,
        eventTimeMs: Long,
    ): HttpResponse {
        return client.post(PERFORMANCE_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("request_time_ms", nowMilliseconds().toString())
                    put(
                        "client_info",
                        buildJsonObject {
                            put("client_type", PERFORMANCE_CLIENT_TYPE_JS)
                            put("js_client_info", buildJsonObject {})
                        }
                    )
                    put("log_source", PERFORMANCE_LOG_SOURCE)
                    put(
                        "log_event",
                        buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("source_extension_json_proto3", event.toString())
                                    put("event_time_ms", eventTimeMs.toString())
                                }
                            )
                        }
                    )
                }
            )
        }
    }
}

class PerformanceTraceJvm(
    internal val name: String,
    private val isCollectionEnabled: () -> Boolean = { true },
    private val onStop: (PerformanceTraceJvm) -> Unit = {},
) : PerformanceTrace {
    private val metrics = ConcurrentHashMap<String, Long>()
    private val attributes = ConcurrentHashMap<String, String>()
    private val startTimeUs = AtomicReference<Long?>()
    private val durationUs = AtomicReference<Long?>()
    private val eventTimeMs = AtomicReference<Long?>()
    private val started = AtomicBoolean(false)
    private val stopped = AtomicBoolean(false)

    override fun start() {
        if (isCollectionEnabled().not()) return

        startTimeUs.set(nowMicroseconds())
        durationUs.set(null)
        eventTimeMs.set(null)
        started.set(true)
        stopped.set(false)
    }

    override fun stop() {
        if (isCollectionEnabled().not() || started.get().not()) return
        if (stopped.compareAndSet(false, true).not()) return

        durationUs.set(elapsedMicroseconds(startTimeUs.get()))
        eventTimeMs.set(nowMilliseconds())
        onStop(this)
    }

    override fun incrementMetric(name: String, by: Long) {
        if (isCollectionEnabled().not()) return

        metrics.merge(name, by) { current, increment -> current + increment }
    }

    override fun putMetric(name: String, value: Long) {
        if (isCollectionEnabled().not()) return

        metrics[name] = value
    }

    override fun getMetric(name: String): Long {
        return metrics[name] ?: 0L
    }

    override fun putAttribute(name: String, value: String) {
        if (isCollectionEnabled().not()) return

        attributes[name] = value
    }

    override fun getAttribute(name: String): String? {
        return attributes[name]
    }

    override fun getAttributes(): Map<String, String> {
        return attributes.toMap()
    }

    override fun removeAttribute(name: String) {
        if (isCollectionEnabled().not()) return

        attributes.remove(name)
    }

    internal fun toFirelogEvent(applicationInfo: JsonObject = firebaseApplicationInfo()): JsonObject {
        val currentStartTimeUs = startTimeUs.get()
        val currentDurationUs = durationUs.get()
        val currentMetrics = metrics.toMap()
        val currentAttributes = attributes.toMap()

        return buildJsonObject {
            put("application_info", applicationInfo)
            put(
                "trace_metric",
                buildJsonObject {
                    put("name", name)
                    put("is_auto", false)
                    put("client_start_time_us", currentStartTimeUs ?: nowMicroseconds())
                    put("duration_us", currentDurationUs ?: 0L)
                    if (currentMetrics.isNotEmpty()) {
                        put(
                            "counters",
                            buildJsonObject {
                                currentMetrics.forEach { (name, value) -> put(name, value) }
                            }
                        )
                    }
                    if (currentAttributes.isNotEmpty()) {
                        put(
                            "custom_attributes",
                            buildJsonObject {
                                currentAttributes.forEach { (name, value) -> put(name, value) }
                            }
                        )
                    }
                }
            )
        }
    }

    internal fun isStarted(): Boolean = started.get()

    internal fun isStopped(): Boolean = stopped.get()

    internal fun getEventTimeMs(): Long = eventTimeMs.get() ?: nowMilliseconds()
}

class PerformanceHttpMetricJvm(
    internal val url: String,
    internal val httpMethod: String,
    private val isCollectionEnabled: () -> Boolean = { true },
    private val onStop: (PerformanceHttpMetricJvm) -> Unit = {},
) : PerformanceHttpMetric {
    private val attributes = ConcurrentHashMap<String, String>()
    private val startTimeUs = AtomicReference<Long?>()
    private val durationUs = AtomicReference<Long?>()
    private val eventTimeMs = AtomicReference<Long?>()
    private val started = AtomicBoolean(false)
    private val stopped = AtomicBoolean(false)
    private val requestPayloadSize = AtomicReference<Long?>()
    private val responsePayloadSize = AtomicReference<Long?>()
    private val httpResponseCode = AtomicReference<Int?>()
    private val responseContentType = AtomicReference<String?>()

    override fun start() {
        if (isCollectionEnabled().not()) return

        startTimeUs.set(nowMicroseconds())
        durationUs.set(null)
        eventTimeMs.set(null)
        started.set(true)
        stopped.set(false)
    }

    override fun stop() {
        if (isCollectionEnabled().not() || started.get().not()) return
        if (stopped.compareAndSet(false, true).not()) return

        durationUs.set(elapsedMicroseconds(startTimeUs.get()))
        eventTimeMs.set(nowMilliseconds())
        onStop(this)
    }

    override fun setRequestPayloadSize(bytes: Long) {
        if (isCollectionEnabled().not()) return

        requestPayloadSize.set(bytes)
    }

    override fun setResponsePayloadSize(bytes: Long) {
        if (isCollectionEnabled().not()) return

        responsePayloadSize.set(bytes)
    }

    override fun setHttpResponseCode(code: Int) {
        if (isCollectionEnabled().not()) return

        httpResponseCode.set(code)
    }

    override fun setResponseContentType(contentType: String?) {
        if (isCollectionEnabled().not()) return

        responseContentType.set(contentType)
    }

    override fun putAttribute(name: String, value: String) {
        if (isCollectionEnabled().not()) return

        attributes[name] = value
    }

    override fun getAttribute(name: String): String? {
        return attributes[name]
    }

    override fun getAttributes(): Map<String, String> {
        return attributes.toMap()
    }

    override fun removeAttribute(name: String) {
        if (isCollectionEnabled().not()) return

        attributes.remove(name)
    }

    internal fun toFirelogEvent(applicationInfo: JsonObject = firebaseApplicationInfo()): JsonObject {
        val currentStartTimeUs = startTimeUs.get()
        val currentDurationUs = durationUs.get()
        val currentAttributes = attributes.toMap()
        val currentRequestPayloadSize = requestPayloadSize.get()
        val currentResponsePayloadSize = responsePayloadSize.get()
        val currentHttpResponseCode = httpResponseCode.get()
        val currentResponseContentType = responseContentType.get()

        return buildJsonObject {
            put("application_info", applicationInfo)
            put(
                "trace_metric",
                buildJsonObject {
                    put("name", HTTP_METRIC_TRACE_NAME)
                    put("is_auto", false)
                    put("client_start_time_us", currentStartTimeUs ?: nowMicroseconds())
                    put("duration_us", currentDurationUs ?: 0L)
                    put(
                        "counters",
                        buildJsonObject {
                            put(HTTP_RESPONSE_CODE_METRIC, currentHttpResponseCode ?: 0)
                            put(REQUEST_PAYLOAD_SIZE_METRIC, currentRequestPayloadSize ?: 0L)
                            put(RESPONSE_PAYLOAD_SIZE_METRIC, currentResponsePayloadSize ?: 0L)
                        }
                    )
                    put(
                        "custom_attributes",
                        buildJsonObject {
                            put(URL_ATTRIBUTE, url.substringBefore("?"))
                            put(HTTP_METHOD_ATTRIBUTE, httpMethod)
                            currentAttributes.forEach { (name, value) -> put(name, value) }
                            currentResponseContentType?.let { put(RESPONSE_CONTENT_TYPE_ATTRIBUTE, it) }
                        }
                    )
                }
            )
        }
    }

    internal fun isStarted(): Boolean = started.get()

    internal fun isStopped(): Boolean = stopped.get()

    internal fun getRequestPayloadSize(): Long? = requestPayloadSize.get()

    internal fun getResponsePayloadSize(): Long? = responsePayloadSize.get()

    internal fun getHttpResponseCode(): Int? = httpResponseCode.get()

    internal fun getResponseContentType(): String? = responseContentType.get()

    internal fun getEventTimeMs(): Long = eventTimeMs.get() ?: nowMilliseconds()

    companion object {
        const val HTTP_METRIC_TRACE_NAME = "http_metric"
        const val URL_ATTRIBUTE = "url"
        const val HTTP_METHOD_ATTRIBUTE = "http_method"
        const val RESPONSE_CONTENT_TYPE_ATTRIBUTE = "response_content_type"
        const val HTTP_RESPONSE_CODE_METRIC = "http_response_code"
        const val REQUEST_PAYLOAD_SIZE_METRIC = "request_payload_size"
        const val RESPONSE_PAYLOAD_SIZE_METRIC = "response_payload_size"
    }
}

private fun firebaseApplicationInfo(): JsonObject {
    val appId = FirebaseJvm.appId ?: throw Exception("Firebase app is not initialized")
    val fid = FirebaseJvm.fid ?: throw Exception("Firebase FID is not set")

    return buildJsonObject {
        put("google_app_id", appId)
        put("app_instance_id", fid)
        put(
            "web_app_info",
            buildJsonObject {
                put("sdk_version", PERFORMANCE_SDK_VERSION)
                put("page_url", "http://desktop:8080/")
                put("service_worker_status", 3) // TODO
                put("visibility_state", 0) // TODO
                put("effective_connection_type", 4) // TODO
            }
        )
        put("application_process_state", 0)
    }
}

private fun nowMilliseconds(): Long = System.currentTimeMillis()

private fun nowMicroseconds(): Long = nowMilliseconds() * 1_000

private fun elapsedMicroseconds(startTimeUs: Long?): Long {
    val start = startTimeUs ?: return 0L
    return (nowMicroseconds() - start).coerceAtLeast(0L)
}
