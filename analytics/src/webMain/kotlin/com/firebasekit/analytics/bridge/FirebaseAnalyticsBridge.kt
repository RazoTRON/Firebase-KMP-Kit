package com.firebasekit.analytics.bridge

import com.firebasekit.core.bridge.FirebaseApp
import com.firebasekit.core.common.models.JsMap

interface AnalyticsWeb {
    fun getAnalytics(app: FirebaseApp): NativeAnalytics
    fun logEvent(analytics: NativeAnalytics, eventName: String, eventParams: JsMap<String, Any>)
    fun setAnalyticsCollectionEnabled(analytics: NativeAnalytics, enabled: Boolean)
    fun setUserId(analytics: NativeAnalytics, id: String?)
    fun setUserProperties(analytics: NativeAnalytics, properties: JsMap<String, Any>)
}

internal class FirebaseAnalyticsBridge : AnalyticsWeb {
    override fun getAnalytics(app: FirebaseApp): NativeAnalytics = nativeGetAnalytics(app)

    override fun logEvent(
        analytics: NativeAnalytics,
        eventName: String,
        eventParams: JsMap<String, Any>,
    ) = nativeLogEvent(analytics, eventName, eventParams.value)

    override fun setAnalyticsCollectionEnabled(analytics: NativeAnalytics, enabled: Boolean) =
        nativeSetAnalyticsCollectionEnabled(analytics, enabled)

    override fun setUserId(analytics: NativeAnalytics, id: String?) =
        nativeSetUserId(analytics, id)

    override fun setUserProperties(analytics: NativeAnalytics, properties: JsMap<String, Any>) =
        nativeSetUserProperties(analytics, properties.value)
}
