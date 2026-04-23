package com.firebasekit.analytics

import com.firebasekit.core.Firebase
import com.firebasekit.core.app
import com.firebasekit.analytics.bridge.AnalyticsWeb
import com.firebasekit.analytics.bridge.FirebaseAnalyticsBridge
import com.firebasekit.analytics.bridge.NativeAnalytics
import com.firebasekit.core.common.models.JsMap
import com.firebasekit.core.common.utils.toJsMap
import kotlin.js.ExperimentalWasmJsInterop

actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalyticsWeb()

@OptIn(ExperimentalWasmJsInterop::class)
class FirebaseAnalyticsWeb(
    private val bridge: AnalyticsWeb = FirebaseAnalyticsBridge(),
) : FirebaseAnalytics {
    private val instance: NativeAnalytics by lazy {
        val currentApp = app ?: throw Exception("Firebase app is not initialized")
        bridge.getAnalytics(currentApp)
    }

    override fun logEvent(name: String, parameters: Bundle) {
        bridge.logEvent(
            analytics = instance,
            eventName = name,
            eventParams = parameters.toJsParams()
        )
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        bridge.setAnalyticsCollectionEnabled(instance, enabled)
    }

    override fun setUserId(userId: String?) {
        bridge.setUserId(instance, userId)
    }

    override fun setUserProperty(name: String, value: String) {
        bridge.setUserProperties(
            analytics = instance,
            properties = mapOf(name to value).toJsMap()
        )
    }

    override fun resetAnalyticsData() {
        // UnsupportedOperationException(UNSUPPORTED_RESET_MESSAGE)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun Bundle.toJsParams(): JsMap<String, Any> {
    return values.mapValues { it.value.value }.toJsMap()
}
