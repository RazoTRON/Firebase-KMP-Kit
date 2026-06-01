@file:JsModule("firebase/analytics")

package com.firebasekit.analytics.bridge

import com.firebasekit.core.bridge.FirebaseApp
import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsName

@JsName("Analytics")
external interface NativeAnalytics : JsAny

@JsName("getAnalytics")
external fun nativeGetAnalytics(app: FirebaseApp): NativeAnalytics

@JsName("logEvent")
external fun nativeLogEvent(
    analyticsInstance: NativeAnalytics,
    eventName: String,
    eventParams: JsAny?,
)

@JsName("setAnalyticsCollectionEnabled")
external fun nativeSetAnalyticsCollectionEnabled(
    analyticsInstance: NativeAnalytics,
    enabled: Boolean,
)

@JsName("setUserId")
external fun nativeSetUserId(
    analyticsInstance: NativeAnalytics,
    id: String?,
)

@JsName("setUserProperties")
external fun nativeSetUserProperties(
    analyticsInstance: NativeAnalytics,
    properties: JsAny,
)
