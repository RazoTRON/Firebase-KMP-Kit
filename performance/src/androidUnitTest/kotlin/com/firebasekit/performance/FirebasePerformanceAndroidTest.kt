package com.firebasekit.performance

import com.google.firebase.perf.FirebasePerformance as AndroidFirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric as AndroidHttpMetric
import com.google.firebase.perf.metrics.Trace as AndroidTrace
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class FirebasePerformanceAndroidTest {

    private val nativePerformance: AndroidFirebasePerformance = mockk(relaxUnitFun = true)
    private val nativeTrace: AndroidTrace = mockk(relaxUnitFun = true)
    private val nativeHttpMetric: AndroidHttpMetric = mockk(relaxUnitFun = true)

    private fun sut() = FirebasePerformanceAndroid(nativePerformance)

    @Test
    fun setPerformanceCollectionEnabled_delegatesToNativeSdk() {
        sut().setPerformanceCollectionEnabled(false)

        verify(exactly = 1) { nativePerformance.isPerformanceCollectionEnabled = false }
    }

    @Test
    fun newTrace_wrapsNativeTrace() {
        every { nativePerformance.newTrace("checkout") } returns nativeTrace

        val trace = sut().newTrace("checkout")

        verify(exactly = 1) { nativePerformance.newTrace("checkout") }
        assertSame(nativeTrace, (trace as PerformanceTraceAndroid).trace)
    }

    @Test
    fun newHttpMetric_wrapsNativeHttpMetric() {
        every {
            nativePerformance.newHttpMetric("https://example.com/products", "GET")
        } returns nativeHttpMetric

        val metric = sut().newHttpMetric("https://example.com/products", "GET")

        verify(exactly = 1) {
            nativePerformance.newHttpMetric("https://example.com/products", "GET")
        }
        assertSame(nativeHttpMetric, (metric as PerformanceHttpMetricAndroid).httpMetric)
    }

    @Test
    fun traceOperations_delegateToNativeTrace() {
        every { nativeTrace.getLongMetric("items") } returns 3
        every { nativeTrace.getAttribute("flow") } returns "checkout"
        every { nativeTrace.attributes } returns mapOf("flow" to "checkout")
        every { nativeTrace.putAttribute("flow", "checkout") } just runs

        val trace = PerformanceTraceAndroid(nativeTrace)

        trace.start()
        trace.incrementMetric("items")
        trace.incrementMetric("items", 2)
        trace.putMetric("items", 3)
        trace.putAttribute("flow", "checkout")
        val metric = trace.getMetric("items")
        val attribute = trace.getAttribute("flow")
        val attributes = trace.getAttributes()
        trace.removeAttribute("flow")
        trace.stop()

        verify(exactly = 1) { nativeTrace.start() }
        verify(exactly = 1) { nativeTrace.incrementMetric("items", 1) }
        verify(exactly = 1) { nativeTrace.incrementMetric("items", 2) }
        verify(exactly = 1) { nativeTrace.putMetric("items", 3) }
        verify(exactly = 1) { nativeTrace.putAttribute("flow", "checkout") }
        verify(exactly = 1) { nativeTrace.getLongMetric("items") }
        verify(exactly = 1) { nativeTrace.getAttribute("flow") }
        verify(exactly = 1) { nativeTrace.attributes }
        verify(exactly = 1) { nativeTrace.removeAttribute("flow") }
        verify(exactly = 1) { nativeTrace.stop() }
        assertEquals(3L, metric)
        assertEquals("checkout", attribute)
        assertEquals(mapOf("flow" to "checkout"), attributes)
    }

    @Test
    fun httpMetricOperations_delegateToNativeHttpMetric() {
        every { nativeHttpMetric.getAttribute("endpoint") } returns "products"
        every { nativeHttpMetric.attributes } returns mapOf("endpoint" to "products")
        every { nativeHttpMetric.putAttribute("endpoint", "products") } just runs

        val metric = PerformanceHttpMetricAndroid(nativeHttpMetric)

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

        verify(exactly = 1) { nativeHttpMetric.start() }
        verify(exactly = 1) { nativeHttpMetric.setRequestPayloadSize(256) }
        verify(exactly = 1) { nativeHttpMetric.setResponsePayloadSize(1024) }
        verify(exactly = 1) { nativeHttpMetric.setHttpResponseCode(200) }
        verify(exactly = 1) { nativeHttpMetric.setResponseContentType("application/json") }
        verify(exactly = 1) { nativeHttpMetric.putAttribute("endpoint", "products") }
        verify(exactly = 1) { nativeHttpMetric.getAttribute("endpoint") }
        verify(exactly = 1) { nativeHttpMetric.attributes }
        verify(exactly = 1) { nativeHttpMetric.removeAttribute("endpoint") }
        verify(exactly = 1) { nativeHttpMetric.stop() }
        assertEquals("products", attribute)
        assertEquals(mapOf("endpoint" to "products"), attributes)
    }
}
