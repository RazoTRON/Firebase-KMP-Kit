@file:JsModule("firebase/performance")

package com.firebasekit.performance.bridge

import com.firebasekit.core.bridge.FirebaseApp
import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsName

@JsName("FirebasePerformance")
external interface NativePerformance : JsAny {
    var dataCollectionEnabled: Boolean
    var instrumentationEnabled: Boolean
}

@JsName("PerformanceTrace")
external interface NativePerformanceTrace : JsAny {
    fun start()
    fun stop()
    fun incrementMetric(metricName: String, num: Double)
    fun putMetric(metricName: String, num: Double)
    fun getMetric(metricName: String): Double
    fun putAttribute(attr: String, value: String)
    fun getAttribute(attr: String): String?
    fun getAttributes(): JsAny
    fun removeAttribute(attr: String)
}

@JsName("getPerformance")
external fun nativeGetPerformance(app: FirebaseApp): NativePerformance

@JsName("trace")
external fun nativeTrace(performance: NativePerformance, name: String): NativePerformanceTrace

