package com.firebasekit.sample

import com.firebasekit.analytics.Bundle
import com.firebasekit.analytics.analytics
import com.firebasekit.core.Firebase
import com.firebasekit.sample.common.Platform
import com.firebasekit.sample.common.current

class AnalyticsViewModel {
    init {
        logSampleEvent()
    }

    private fun logSampleEvent() {
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        Firebase.analytics.logEvent(
            name = "sample_app_open",
            parameters = Bundle().apply {
                put("platform", Platform.current().name)
            }
        )
    }
}