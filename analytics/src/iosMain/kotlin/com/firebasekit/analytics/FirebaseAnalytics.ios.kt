package com.firebasekit.analytics

import com.firebasekit.core.Firebase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

actual val Firebase.analytics: FirebaseAnalytics by lazy { FirebaseAnalyticsIos() }

class FirebaseAnalyticsIos(
    private val analytics: AnalyticsBridge = FIRAnalyticsBridge(),
) : FirebaseAnalytics {
    override fun logEvent(name: String, parameters: Bundle) {
        analytics.logEvent(name, parameters.toNativeParameters())
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    override fun resetAnalyticsData() {
        analytics.resetAnalyticsData()
    }
}

private fun Bundle.toNativeParameters(): Map<Any?, *>? {
    if (values.isEmpty()) return null

    return buildMap {
        this@toNativeParameters.values.forEach { put(it.key, it.value.toNativeValue()) }
    }
}

private fun BundleValue.toNativeValue(): Any = when (this) {
    is BundleValue.StringValue -> value
    is BundleValue.IntValue -> value
    is BundleValue.FloatValue -> value
    is BundleValue.LongValue -> value
    is BundleValue.DoubleValue -> value
    is BundleValue.BooleanValue -> if (value) 1L else 0L
    is BundleValue.SerializableValue -> Json.encodeToString(value)
}
