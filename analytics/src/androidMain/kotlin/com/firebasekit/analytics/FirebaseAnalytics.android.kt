package com.firebasekit.analytics

import android.os.Bundle as AndroidBundle
import com.firebasekit.core.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics as AndroidFirebaseAnalytics
import kotlinx.serialization.json.Json

actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalyticsAndroid()

class FirebaseAnalyticsAndroid(
    private val analytics: AndroidFirebaseAnalytics = AndroidFirebaseAnalytics.getInstance(
        FirebaseApp.getInstance().applicationContext
    ),
) : FirebaseAnalytics {
    override fun logEvent(name: String, parameters: Bundle) {
        analytics.logEvent(name, parameters.toAndroidBundle())
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

private fun Bundle.toAndroidBundle(): AndroidBundle? {
    if (values.isEmpty()) return null

    return AndroidBundle().apply {
        values.forEach { putAnalyticsParameter(it.key, it.value) }
    }
}

private fun AndroidBundle.putAnalyticsParameter(
    key: String,
    value: BundleValue,
) {
    when (value) {
        is BundleValue.StringValue -> putString(key, value.value)
        is BundleValue.LongValue -> putLong(key, value.value)
        is BundleValue.DoubleValue -> putDouble(key, value.value)
        is BundleValue.BooleanValue -> putLong(key, if (value.value) 1L else 0L)
        is BundleValue.SerializableValue -> putString(key, Json.encodeToString(value))
    }
}