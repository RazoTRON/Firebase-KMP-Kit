package com.firebasekit.sample

import com.firebasekit.core.Firebase
import com.firebasekit.performance.performance
import com.firebasekit.sample.common.Platform
import com.firebasekit.sample.common.current
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val statusMessage: String = "Ready to record a Firebase Performance sample.",
    val lastTraceName: String = "",
    val isRunning: Boolean = false,
)

class PerformanceViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(
        PerformanceUiState(
            statusMessage = "Ready to record a Firebase Performance sample."
        )
    )
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    fun recordTrace() {
        if (uiState.value.isRunning) return

        scope.launch {
            _uiState.update { it.copy(isRunning = true, statusMessage = "Recording custom trace...") }

            val result = runCatching { recordPerformanceTraceSample() }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { sample ->
                        current.copy(
                            isRunning = false,
                            statusMessage = sample.message,
                            lastTraceName = sample.name,
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isRunning = false,
                            statusMessage = error.message ?: "Failed to record the custom trace.",
                        )
                    }
                )
            }
        }
    }

    fun recordHttpMetric() {
        if (uiState.value.isRunning) return

        scope.launch {
            _uiState.update { it.copy(isRunning = true, statusMessage = "Recording HTTP metric...") }

            val result = runCatching { recordPerformanceHttpMetricSample() }

            _uiState.update { current ->
                result.fold(
                    onSuccess = { sample ->
                        current.copy(
                            isRunning = false,
                            statusMessage = sample.message,
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isRunning = false,
                            statusMessage = error.message ?: "Failed to record the HTTP metric.",
                        )
                    }
                )
            }
        }
    }

    internal fun recordPerformanceTraceSample(): PerformanceSampleResult {
        Firebase.performance.setPerformanceCollectionEnabled(true)

        val trace = Firebase.performance.newTrace(SAMPLE_TRACE_NAME)
        trace.start()
        trace.putAttribute("source", "sample_app")
        trace.incrementMetric("button_taps")
        trace.putMetric("sample_items", 3)
        trace.stop()

        return PerformanceSampleResult(
            name = SAMPLE_TRACE_NAME,
            message = "Recorded trace '$SAMPLE_TRACE_NAME' with attributes and metrics.",
        )
    }

    internal fun recordPerformanceHttpMetricSample(): PerformanceSampleResult {
        Firebase.performance.setPerformanceCollectionEnabled(true)

        val metric = Firebase.performance.newHttpMetric(SAMPLE_HTTP_METRIC_URL, "GET")
        metric.start()
        metric.putAttribute("source", "sample_app")
        metric.setHttpResponseCode(200)
        metric.setRequestPayloadSize(0)
        metric.setResponsePayloadSize(2048)
        metric.setResponseContentType("text/html")
        metric.stop()

        return PerformanceSampleResult(
            name = SAMPLE_HTTP_METRIC_URL,
            message = "Recorded HTTP metric for Firebase Performance docs.",
        )
    }

    companion object {
        private const val SAMPLE_TRACE_NAME = "sample_app_trace"
        private const val SAMPLE_HTTP_METRIC_URL = "https://firebase.google.com/docs/perf-mon"
    }
}

internal data class PerformanceSampleResult(
    val name: String,
    val message: String,
)