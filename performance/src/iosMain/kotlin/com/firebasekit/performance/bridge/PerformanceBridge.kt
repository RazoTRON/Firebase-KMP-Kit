package com.firebasekit.performance.bridge

import platform.Foundation.NSURL
import swiftPMImport.com.firebasekit.performance.FIRHTTPMethod
import swiftPMImport.com.firebasekit.performance.FIRHTTPMetric
import swiftPMImport.com.firebasekit.performance.FIRPerformance

interface PerformanceBridge {
    fun setPerformanceCollectionEnabled(enabled: Boolean)
    fun newTrace(name: String): TraceBridge?
    fun newHttpMetric(url: String, httpMethod: String): HttpMetricBridge?
}

class FIRPerformanceBridge(
    private val native: FIRPerformance = FIRPerformance.sharedInstance(),
) : PerformanceBridge {
    override fun setPerformanceCollectionEnabled(enabled: Boolean) {
        native.dataCollectionEnabled = enabled
    }

    override fun newTrace(name: String): TraceBridge? {
        return native.traceWithName(name)?.let(::FIRTraceBridge)
    }

    override fun newHttpMetric(url: String, httpMethod: String): HttpMetricBridge? {
        val nsUrl = NSURL.URLWithString(url) ?: return null
        val httpMetric = FIRHTTPMetric(uRL = nsUrl, HTTPMethod = httpMethod.toFirHttpMethod())
        return httpMetric.let(::FIRHttpMetricBridge)
    }
}


private fun String.toFirHttpMethod(): FIRHTTPMethod = when (uppercase()) {
    "GET" -> FIRHTTPMethod.FIRHTTPMethodGET
    "PUT" -> FIRHTTPMethod.FIRHTTPMethodPUT
    "POST" -> FIRHTTPMethod.FIRHTTPMethodPOST
    "DELETE" -> FIRHTTPMethod.FIRHTTPMethodDELETE
    "HEAD" -> FIRHTTPMethod.FIRHTTPMethodHEAD
    "PATCH" -> FIRHTTPMethod.FIRHTTPMethodPATCH
    "OPTIONS" -> FIRHTTPMethod.FIRHTTPMethodOPTIONS
    "TRACE" -> FIRHTTPMethod.FIRHTTPMethodTRACE
    "CONNECT" -> FIRHTTPMethod.FIRHTTPMethodCONNECT
    else -> throw IllegalArgumentException("Unsupported HTTP method: $this")
}
