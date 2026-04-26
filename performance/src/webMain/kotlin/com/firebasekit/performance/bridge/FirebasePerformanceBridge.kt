package com.firebasekit.performance.bridge

import com.firebasekit.core.bridge.FirebaseApp
import com.firebasekit.core.common.models.JsMap

interface PerformanceWeb {
    fun getPerformance(app: FirebaseApp): NativePerformance
    fun setPerformanceCollectionEnabled(performance: NativePerformance, enabled: Boolean)
    fun newTrace(performance: NativePerformance, name: String): NativePerformanceTrace
    fun startTrace(trace: NativePerformanceTrace)
    fun stopTrace(trace: NativePerformanceTrace)
    fun incrementMetric(trace: NativePerformanceTrace, name: String, by: Long)
    fun putMetric(trace: NativePerformanceTrace, name: String, value: Long)
    fun getMetric(trace: NativePerformanceTrace, name: String): Double
    fun putAttribute(trace: NativePerformanceTrace, name: String, value: String)
    fun getAttribute(trace: NativePerformanceTrace, name: String): String?
    fun getAttributes(trace: NativePerformanceTrace): JsMap<String, String>
    fun removeAttribute(trace: NativePerformanceTrace, name: String)
}

internal class FirebasePerformanceBridge : PerformanceWeb {
    override fun getPerformance(app: FirebaseApp): NativePerformance = nativeGetPerformance(app)

    override fun setPerformanceCollectionEnabled(performance: NativePerformance, enabled: Boolean) {
        performance.dataCollectionEnabled = enabled
        performance.instrumentationEnabled = enabled
    }

    override fun newTrace(performance: NativePerformance, name: String): NativePerformanceTrace =
        nativeTrace(performance, name)

    override fun startTrace(trace: NativePerformanceTrace) {
        trace.start()
    }

    override fun stopTrace(trace: NativePerformanceTrace) {
        trace.stop()
    }

    override fun incrementMetric(trace: NativePerformanceTrace, name: String, by: Long) {
        trace.incrementMetric(name, by.toDouble())
    }

    override fun putMetric(trace: NativePerformanceTrace, name: String, value: Long) {
        trace.putMetric(name, value.toDouble())
    }

    override fun getMetric(trace: NativePerformanceTrace, name: String): Double =
        trace.getMetric(name)

    override fun putAttribute(trace: NativePerformanceTrace, name: String, value: String) {
        trace.putAttribute(name, value)
    }

    override fun getAttribute(trace: NativePerformanceTrace, name: String): String? =
        trace.getAttribute(name)

    override fun getAttributes(trace: NativePerformanceTrace): JsMap<String, String> =
        JsMap(trace.getAttributes())

    override fun removeAttribute(trace: NativePerformanceTrace, name: String) {
        trace.removeAttribute(name)
    }
}

