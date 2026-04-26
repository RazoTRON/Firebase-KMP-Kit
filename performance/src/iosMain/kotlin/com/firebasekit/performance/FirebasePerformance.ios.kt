package com.firebasekit.performance

import com.firebasekit.core.Firebase
import com.firebasekit.performance.bridge.FIRPerformanceBridge
import com.firebasekit.performance.bridge.HttpMetricBridge
import com.firebasekit.performance.bridge.PerformanceBridge
import com.firebasekit.performance.bridge.TraceBridge

actual val Firebase.performance: FirebasePerformance by lazy { FirebasePerformanceIos() }

class FirebasePerformanceIos(
    private val performance: PerformanceBridge = FIRPerformanceBridge(),
) : FirebasePerformance {
    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        performance.setPerformanceCollectionEnabled(enabled)
    }

    override fun newTrace(name: String): PerformanceTrace {
        return PerformanceTraceIos(
            performance.newTrace(name)
                ?: throw IllegalArgumentException("Unable to create performance trace: $name")
        )
    }

    override fun newHttpMetric(url: String, httpMethod: String): PerformanceHttpMetric {
        return PerformanceHttpMetricIos(
            performance.newHttpMetric(url, httpMethod)
                ?: throw IllegalArgumentException("Unable to create HTTP metric for URL: $url")
        )
    }
}

class PerformanceTraceIos(
    internal val trace: TraceBridge,
) : PerformanceTrace {
    override fun start() {
        trace.start()
    }

    override fun stop() {
        trace.stop()
    }

    override fun incrementMetric(name: String, by: Long) {
        trace.incrementMetric(name, by)
    }

    override fun putMetric(name: String, value: Long) {
        trace.putMetric(name, value)
    }

    override fun getMetric(name: String): Long {
        return trace.getMetric(name)
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
}

class PerformanceHttpMetricIos(
    internal val httpMetric: HttpMetricBridge,
) : PerformanceHttpMetric {
    override fun start() {
        httpMetric.start()
    }

    override fun stop() {
        httpMetric.stop()
    }

    override fun setRequestPayloadSize(bytes: Long) {
        httpMetric.setRequestPayloadSize(bytes)
    }

    override fun setResponsePayloadSize(bytes: Long) {
        httpMetric.setResponsePayloadSize(bytes)
    }

    override fun setHttpResponseCode(code: Int) {
        httpMetric.setHttpResponseCode(code)
    }

    override fun setResponseContentType(contentType: String?) {
        httpMetric.setResponseContentType(contentType)
    }

    override fun putAttribute(name: String, value: String) {
        httpMetric.putAttribute(name, value)
    }

    override fun getAttribute(name: String): String? {
        return httpMetric.getAttribute(name)
    }

    override fun getAttributes(): Map<String, String> {
        return httpMetric.getAttributes()
    }

    override fun removeAttribute(name: String) {
        httpMetric.removeAttribute(name)
    }
}
