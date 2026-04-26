package com.firebasekit.performance

import com.firebasekit.performance.bridge.HttpMetricBridge
import com.firebasekit.performance.bridge.PerformanceBridge
import com.firebasekit.performance.bridge.TraceBridge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FirebasePerformanceIosTest {

    private class FakePerformanceBridge : PerformanceBridge {
        var collectionEnabled: Boolean? = null
        var traceName: String? = null
        var metricUrl: String? = null
        var metricMethod: String? = null
        var trace: TraceBridge? = FakeTraceBridge()
        var httpMetric: HttpMetricBridge? = FakeHttpMetricBridge()

        override fun setPerformanceCollectionEnabled(enabled: Boolean) {
            collectionEnabled = enabled
        }

        override fun newTrace(name: String): TraceBridge? {
            traceName = name
            return trace
        }

        override fun newHttpMetric(url: String, httpMethod: String): HttpMetricBridge? {
            metricUrl = url
            metricMethod = httpMethod
            return httpMetric
        }
    }

    private class FakeTraceBridge : TraceBridge {
        var starts = 0
        var stops = 0
        var incrementName: String? = null
        var incrementBy: Long? = null
        var metricName: String? = null
        var metricValue: Long? = null
        var attributeName: String? = null
        var attributeValue: String? = null
        var removedAttributeName: String? = null

        override fun start() {
            starts += 1
        }

        override fun stop() {
            stops += 1
        }

        override fun incrementMetric(name: String, by: Long) {
            incrementName = name
            incrementBy = by
        }

        override fun putMetric(name: String, value: Long) {
            metricName = name
            metricValue = value
        }

        override fun getMetric(name: String): Long = 42

        override fun putAttribute(name: String, value: String) {
            attributeName = name
            attributeValue = value
        }

        override fun getAttribute(name: String): String? = "checkout"

        override fun getAttributes(): Map<String, String> = mapOf("flow" to "checkout")

        override fun removeAttribute(name: String) {
            removedAttributeName = name
        }
    }

    private class FakeHttpMetricBridge : HttpMetricBridge {
        var starts = 0
        var stops = 0
        var requestPayloadSize: Long? = null
        var responsePayloadSize: Long? = null
        var responseCode: Int? = null
        var responseContentType: String? = null
        var attributeName: String? = null
        var attributeValue: String? = null
        var removedAttributeName: String? = null

        override fun start() {
            starts += 1
        }

        override fun stop() {
            stops += 1
        }

        override fun setRequestPayloadSize(bytes: Long) {
            requestPayloadSize = bytes
        }

        override fun setResponsePayloadSize(bytes: Long) {
            responsePayloadSize = bytes
        }

        override fun setHttpResponseCode(code: Int) {
            responseCode = code
        }

        override fun setResponseContentType(contentType: String?) {
            responseContentType = contentType
        }

        override fun putAttribute(name: String, value: String) {
            attributeName = name
            attributeValue = value
        }

        override fun getAttribute(name: String): String? = "products"

        override fun getAttributes(): Map<String, String> = mapOf("endpoint" to "products")

        override fun removeAttribute(name: String) {
            removedAttributeName = name
        }
    }

    private fun sut(bridge: FakePerformanceBridge = FakePerformanceBridge()) =
        FirebasePerformanceIos(bridge)

    @Test
    fun setPerformanceCollectionEnabled_delegatesToBridge() {
        val bridge = FakePerformanceBridge()

        sut(bridge).setPerformanceCollectionEnabled(false)

        assertEquals(false, bridge.collectionEnabled)
    }

    @Test
    fun newTrace_wrapsBridgeTrace() {
        val bridge = FakePerformanceBridge()

        val trace = sut(bridge).newTrace("checkout")

        assertEquals("checkout", bridge.traceName)
        assertSame(bridge.trace, (trace as PerformanceTraceIos).trace)
    }

    @Test
    fun newTrace_throwsWhenNativeTraceCannotBeCreated() {
        val bridge = FakePerformanceBridge().apply { trace = null }

        assertFailsWith<IllegalArgumentException> {
            sut(bridge).newTrace("")
        }
    }

    @Test
    fun newHttpMetric_wrapsBridgeMetric() {
        val bridge = FakePerformanceBridge()

        val metric = sut(bridge).newHttpMetric("https://example.com/products", "GET")

        assertEquals("https://example.com/products", bridge.metricUrl)
        assertEquals("GET", bridge.metricMethod)
        assertSame(bridge.httpMetric, (metric as PerformanceHttpMetricIos).httpMetric)
    }

    @Test
    fun newHttpMetric_throwsWhenNativeMetricCannotBeCreated() {
        val bridge = FakePerformanceBridge().apply { httpMetric = null }

        assertFailsWith<IllegalArgumentException> {
            sut(bridge).newHttpMetric("not-a-url", "GET")
        }
    }

    @Test
    fun traceOperations_delegateToBridgeTrace() {
        val bridge = FakeTraceBridge()
        val trace = PerformanceTraceIos(bridge)

        trace.start()
        trace.incrementMetric("items")
        trace.putMetric("items", 3)
        trace.putAttribute("flow", "checkout")
        val metric = trace.getMetric("items")
        val attribute = trace.getAttribute("flow")
        val attributes = trace.getAttributes()
        trace.removeAttribute("flow")
        trace.stop()

        assertEquals(1, bridge.starts)
        assertEquals("items", bridge.incrementName)
        assertEquals(1, bridge.incrementBy)
        assertEquals("items", bridge.metricName)
        assertEquals(3, bridge.metricValue)
        assertEquals("flow", bridge.attributeName)
        assertEquals("checkout", bridge.attributeValue)
        assertEquals(42, metric)
        assertEquals("checkout", attribute)
        assertEquals(mapOf("flow" to "checkout"), attributes)
        assertEquals("flow", bridge.removedAttributeName)
        assertEquals(1, bridge.stops)
    }

    @Test
    fun httpMetricOperations_delegateToBridgeMetric() {
        val bridge = FakeHttpMetricBridge()
        val metric = PerformanceHttpMetricIos(bridge)

        metric.start()
        metric.setRequestPayloadSize(256)
        metric.setResponsePayloadSize(1024)
        metric.setHttpResponseCode(200)
        metric.setResponseContentType("application/json")
        metric.putAttribute("endpoint", "products")
        val attribute = metric.getAttribute("endpoint")
        val attributes = metric.getAttributes()
        metric.removeAttribute("endpoint")
        metric.stop()

        assertEquals(1, bridge.starts)
        assertEquals(256, bridge.requestPayloadSize)
        assertEquals(1024, bridge.responsePayloadSize)
        assertEquals(200, bridge.responseCode)
        assertEquals("application/json", bridge.responseContentType)
        assertEquals("endpoint", bridge.attributeName)
        assertEquals("products", bridge.attributeValue)
        assertEquals("products", attribute)
        assertEquals(mapOf("endpoint" to "products"), attributes)
        assertEquals("endpoint", bridge.removedAttributeName)
        assertEquals(1, bridge.stops)
    }
}
