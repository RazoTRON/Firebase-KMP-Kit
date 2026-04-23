package com.firebasekit.analytics

import com.firebasekit.core.Firebase

actual val Firebase.analytics: FirebaseAnalytics
    get() = UnsupportedFirebaseAnalytics

private object UnsupportedFirebaseAnalytics : FirebaseAnalytics {
    override fun logEvent(name: String, parameters: Bundle) =
        unsupportedAnalytics()

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) = unsupportedAnalytics()

    override fun setUserId(userId: String?) = unsupportedAnalytics()

    override fun setUserProperty(name: String, value: String?) = unsupportedAnalytics()

    override fun resetAnalyticsData() = unsupportedAnalytics()
}
