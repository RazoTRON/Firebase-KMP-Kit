package com.firebasekit.performance

import com.firebasekit.core.Firebase
import com.google.firebase.perf.FirebasePerformance as AndroidFirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric as AndroidHttpMetric
import com.google.firebase.perf.metrics.Trace as AndroidTrace

actual val Firebase.performance: FirebasePerformance by lazy { FirebasePerformanceAndroid() }

class FirebasePerformanceAndroid(
    private val performance: AndroidFirebasePerformance = AndroidFirebasePerformance.getInstance(),
) : FirebasePerformance {
    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        performance.isPerformanceCollectionEnabled = enabled
    }

    override fun newTrace(name: String): PerformanceTrace {
        return PerformanceTraceAndroid(performance.newTrace(name))
    }

    override fun newHttpMetric(url: String, httpMethod: String): PerformanceHttpMetric {
        return PerformanceHttpMetricAndroid(performance.newHttpMetric(url, httpMethod))
    }
}

class PerformanceTraceAndroid(
    internal val trace: AndroidTrace,
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
        return trace.getLongMetric(name)
    }

    override fun putAttribute(name: String, value: String) {
        trace.putAttribute(name, value)
    }

    override fun getAttribute(name: String): String? {
        return trace.getAttribute(name)
    }

    override fun getAttributes(): Map<String, String> {
        return trace.attributes
    }

    override fun removeAttribute(name: String) {
        trace.removeAttribute(name)
    }
}

class PerformanceHttpMetricAndroid(
    internal val httpMetric: AndroidHttpMetric,
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
        return httpMetric.attributes
    }

    override fun removeAttribute(name: String) {
        httpMetric.removeAttribute(name)
    }
}
