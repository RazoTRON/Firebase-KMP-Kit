package com.firebasekit.analytics

import swiftPMImport.com.firebasekit.analytics.FIRAnalytics

interface AnalyticsBridge {
    fun logEvent(name: String, parameters: Map<Any?, *>?)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun resetAnalyticsData()
}

class FIRAnalyticsBridge : AnalyticsBridge {
    override fun logEvent(name: String, parameters: Map<Any?, *>?) {
        FIRAnalytics.logEventWithName(name, parameters)
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        FIRAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        FIRAnalytics.setUserID(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        FIRAnalytics.setUserPropertyString(value, name)
    }

    override fun resetAnalyticsData() {
        FIRAnalytics.resetAnalyticsData()
    }
}
