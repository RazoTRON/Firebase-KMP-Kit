package com.firebasekit.performance

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FirebasePerformanceJvmTest {

    @Test
    fun newTrace_returnsJvmTrace() {
        val trace = FirebasePerformanceJvm().newTrace("checkout")

        assertIs<PerformanceTraceJvm>(trace)
        assertEquals("checkout", trace.name)
    }

    @Test
    fun traceOperations_storeLocalState() {
        val trace = PerformanceTraceJvm("checkout")

        trace.start()
        trace.incrementMetric("items")
        trace.incrementMetric("items", 2)
        trace.putMetric("total", 42)
        trace.putAttribute("flow", "checkout")
        trace.putAttribute("source", "desktop")
        trace.removeAttribute("source")
        trace.stop()

        assertTrue(trace.isStarted())
        assertTrue(trace.isStopped())
        assertEquals(3L, trace.getMetric("items"))
        assertEquals(42L, trace.getMetric("total"))
        assertEquals(0L, trace.getMetric("missing"))
        assertEquals("checkout", trace.getAttribute("flow"))
        assertEquals(mapOf("flow" to "checkout"), trace.getAttributes())
        assertNull(trace.getAttribute("source"))
    }

    @Test
    fun newHttpMetric_returnsJvmMetric() {
        val metric = FirebasePerformanceJvm().newHttpMetric("https://example.com/products", "GET")

        assertIs<PerformanceHttpMetricJvm>(metric)
        assertEquals("https://example.com/products", metric.url)
        assertEquals("GET", metric.httpMethod)
    }

    @Test
    fun httpMetricOperations_storeLocalState() {
        val metric = PerformanceHttpMetricJvm("https://example.com/products", "GET")

        metric.start()
        metric.setRequestPayloadSize(256)
        metric.setResponsePayloadSize(1024)
        metric.setHttpResponseCode(200)
        metric.setResponseContentType("application/json")
        metric.putAttribute("endpoint", "products")
        metric.putAttribute("source", "desktop")
        metric.removeAttribute("source")
        metric.stop()

        assertTrue(metric.isStarted())
        assertTrue(metric.isStopped())
        assertEquals(256L, metric.getRequestPayloadSize())
        assertEquals(1024L, metric.getResponsePayloadSize())
        assertEquals(200, metric.getHttpResponseCode())
        assertEquals("application/json", metric.getResponseContentType())
        assertEquals("products", metric.getAttribute("endpoint"))
        assertEquals(mapOf("endpoint" to "products"), metric.getAttributes())
        assertNull(metric.getAttribute("source"))
    }

    @Test
    fun disabledCollection_ignoresMutations() {
        val performance = FirebasePerformanceJvm()
        performance.setPerformanceCollectionEnabled(false)

        val trace = performance.newTrace("checkout") as PerformanceTraceJvm
        trace.start()
        trace.putMetric("items", 3)
        trace.putAttribute("flow", "checkout")
        trace.stop()

        val metric = performance.newHttpMetric("https://example.com/products", "GET") as PerformanceHttpMetricJvm
        metric.start()
        metric.setHttpResponseCode(200)
        metric.putAttribute("endpoint", "products")
        metric.stop()

        assertFalse(trace.isStarted())
        assertFalse(trace.isStopped())
        assertEquals(0L, trace.getMetric("items"))
        assertTrue(trace.getAttributes().isEmpty())
        assertFalse(metric.isStarted())
        assertFalse(metric.isStopped())
        assertNull(metric.getHttpResponseCode())
        assertTrue(metric.getAttributes().isEmpty())
    }

    @Test
    fun httpMetricFirelogEvent_usesHttpMetricTraceShape() {
        val metric = PerformanceHttpMetricJvm("https://firebase.google.com/docs/perf-mon?hl=en", "GET")

        metric.start()
        metric.putAttribute("source", "sample_app")
        metric.setHttpResponseCode(200)
        metric.setRequestPayloadSize(0)
        metric.setResponsePayloadSize(2048)
        metric.setResponseContentType("text/html")
        metric.stop()

        val traceMetric = metric.toFirelogEvent(fakeApplicationInfo())["trace_metric"]!!.jsonObject
        val counters = traceMetric["counters"]!!.jsonObject
        val attributes = traceMetric["custom_attributes"]!!.jsonObject

        assertEquals("http_metric", traceMetric["name"]!!.jsonPrimitive.content)
        assertEquals(false, traceMetric["is_auto"]!!.jsonPrimitive.content.toBoolean())
        assertEquals(0L, traceMetric["duration_us"]!!.jsonPrimitive.long)
        assertEquals(200, counters["http_response_code"]!!.jsonPrimitive.int)
        assertEquals(0L, counters["request_payload_size"]!!.jsonPrimitive.long)
        assertEquals(2048L, counters["response_payload_size"]!!.jsonPrimitive.long)
        assertEquals("https://firebase.google.com/docs/perf-mon", attributes["url"]!!.jsonPrimitive.content)
        assertEquals("GET", attributes["http_method"]!!.jsonPrimitive.content)
        assertEquals("sample_app", attributes["source"]!!.jsonPrimitive.content)
        assertEquals("text/html", attributes["response_content_type"]!!.jsonPrimitive.content)
    }

    private fun fakeApplicationInfo(): JsonObject = buildJsonObject {
        put("google_app_id", "1:388792860519:web:cd3070888b06d607be39c2")
        put("app_instance_id", "cq_Pek91ljz_V98tFqhZtJ")
    }
}
