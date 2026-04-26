package com.firebasekit.performance

import com.firebasekit.core.Firebase

expect val Firebase.performance: FirebasePerformance

interface FirebasePerformance {
    fun setPerformanceCollectionEnabled(enabled: Boolean)
    fun newTrace(name: String): PerformanceTrace
    fun newHttpMetric(url: String, httpMethod: String): PerformanceHttpMetric
}

interface PerformanceTrace {
    fun start()
    fun stop()
    fun incrementMetric(name: String, by: Long = 1)
    fun putMetric(name: String, value: Long)
    fun getMetric(name: String): Long
    fun putAttribute(name: String, value: String)
    fun getAttribute(name: String): String?
    fun getAttributes(): Map<String, String>
    fun removeAttribute(name: String)
}

interface PerformanceHttpMetric {
    fun start()
    fun stop()
    fun setRequestPayloadSize(bytes: Long)
    fun setResponsePayloadSize(bytes: Long)
    fun setHttpResponseCode(code: Int)
    fun setResponseContentType(contentType: String?)
    fun putAttribute(name: String, value: String)
    fun getAttribute(name: String): String?
    fun getAttributes(): Map<String, String>
    fun removeAttribute(name: String)
}
