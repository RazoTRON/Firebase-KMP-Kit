package com.firebasekit.performance

import com.firebasekit.core.Firebase
import com.firebasekit.core.app
import com.firebasekit.performance.bridge.FirebasePerformanceBridge
import com.firebasekit.performance.bridge.NativePerformance
import com.firebasekit.performance.bridge.NativePerformanceTrace
import com.firebasekit.performance.bridge.PerformanceWeb
import com.firebasekit.performance.bridge.toKotlinStringMap
import kotlin.js.ExperimentalWasmJsInterop

actual val Firebase.performance: FirebasePerformance by lazy { FirebasePerformanceWeb() }

@OptIn(ExperimentalWasmJsInterop::class)
class FirebasePerformanceWeb(
    private val bridge: PerformanceWeb = FirebasePerformanceBridge(),
) : FirebasePerformance {
    private val instance: NativePerformance by lazy {
        val currentApp = app ?: throw Exception("Firebase app is not initialized")
        bridge.getPerformance(currentApp)
    }

    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        bridge.setPerformanceCollectionEnabled(instance, enabled)
    }

    override fun newTrace(name: String): PerformanceTrace {
        return PerformanceTraceWeb(bridge.newTrace(instance, name), bridge)
    }

    override fun newHttpMetric(url: String, httpMethod: String): PerformanceHttpMetric {
        return PerformanceHttpMetricWeb(
            trace = PerformanceTraceWeb(
                trace = bridge.newTrace(
                    performance = instance,
                    name = PerformanceHttpMetricWeb.HTTP_METRIC_TRACE_NAME
                ),
                bridge = bridge
            ),
            url = url,
            httpMethod = httpMethod,
        )
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
class PerformanceTraceWeb(
    private val trace: NativePerformanceTrace,
    private val bridge: PerformanceWeb,
) : PerformanceTrace {
    override fun start() {
        bridge.startTrace(trace)
    }

    override fun stop() {
        bridge.stopTrace(trace)
    }

    override fun incrementMetric(name: String, by: Long) {
        bridge.incrementMetric(trace, name, by)
    }

    override fun putMetric(name: String, value: Long) {
        bridge.putMetric(trace, name, value)
    }

    override fun getMetric(name: String): Long {
        return bridge.getMetric(trace, name).toLong()
    }

    override fun putAttribute(name: String, value: String) {
        bridge.putAttribute(trace, name, value)
    }

    override fun getAttribute(name: String): String? {
        return bridge.getAttribute(trace, name)
    }

    override fun getAttributes(): Map<String, String> {
        return bridge.getAttributes(trace).toKotlinStringMap()
    }

    override fun removeAttribute(name: String) {
        bridge.removeAttribute(trace, name)
    }
}

class PerformanceHttpMetricWeb(
    private val trace: PerformanceTraceWeb,
    url: String,
    httpMethod: String,
) : PerformanceHttpMetric {
    init {
        trace.putAttribute(URL_ATTRIBUTE, url)
        trace.putAttribute(HTTP_METHOD_ATTRIBUTE, httpMethod)
    }

    override fun start() {
        trace.start()
    }

    override fun stop() {
        trace.stop()
    }

    override fun setRequestPayloadSize(bytes: Long) {
        trace.putMetric(REQUEST_PAYLOAD_SIZE_METRIC, bytes)
    }

    override fun setResponsePayloadSize(bytes: Long) {
        trace.putMetric(RESPONSE_PAYLOAD_SIZE_METRIC, bytes)
    }

    override fun setHttpResponseCode(code: Int) {
        trace.putMetric(HTTP_RESPONSE_CODE_METRIC, code.toLong())
    }

    override fun setResponseContentType(contentType: String?) {
        if (contentType == null) {
            trace.removeAttribute(RESPONSE_CONTENT_TYPE_ATTRIBUTE)
        } else {
            trace.putAttribute(RESPONSE_CONTENT_TYPE_ATTRIBUTE, contentType)
        }
    }

    override fun putAttribute(name: String, value: String) {
        trace.putAttribute(name, value)
    }

    override fun getAttribute(name: String): String? {
        return trace.getAttribute(name)
    }

    override fun getAttributes(): Map<String, String> {
        return trace.getAttributes()
    }

    override fun removeAttribute(name: String) {
        trace.removeAttribute(name)
    }

    companion object {
        const val HTTP_METRIC_TRACE_NAME = "http_metric"
        const val URL_ATTRIBUTE = "url"
        const val HTTP_METHOD_ATTRIBUTE = "http_method"
        const val RESPONSE_CONTENT_TYPE_ATTRIBUTE = "response_content_type"
        const val REQUEST_PAYLOAD_SIZE_METRIC = "request_payload_size"
        const val RESPONSE_PAYLOAD_SIZE_METRIC = "response_payload_size"
        const val HTTP_RESPONSE_CODE_METRIC = "http_response_code"
    }
}
