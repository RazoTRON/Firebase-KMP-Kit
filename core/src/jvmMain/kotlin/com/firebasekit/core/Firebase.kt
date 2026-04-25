package com.firebasekit.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

suspend fun Firebase.initialize(
    apiKey: String,
    projectId: String,
    appId: String,
    analyticsApiSecret: String? = null,
    measurementId: String? = null,
    interval: Duration = 60.minutes,
    cacheFilePath: String = "cache/firebase_data"
) {
    FirebaseJvm.initialize(
        apiKey = apiKey,
        projectId = projectId,
        appId = appId,
        analyticsApiSecret = analyticsApiSecret,
        measurementId = measurementId,
        intervalSeconds = interval,
        cacheFilePath = cacheFilePath
    )
}

object FirebaseJvm {
    private val dispatcher = Dispatchers.IO

    var apiKey: String? = null
        private set
    var projectId: String? = null
        private set
    var appId: String? = null
        private set
    var analyticsApiSecret: String? = null
        private set
    var measurementId: String? = null
        private set
    var interval: Duration? = null
        private set
    var clientId: String? = null
        private set
    var fid: String? = null

    private val firebaseCache = FirebaseCache()

    internal suspend fun initialize(
        apiKey: String,
        projectId: String,
        appId: String,
        analyticsApiSecret: String?,
        measurementId: String?,
        intervalSeconds: Duration,
        cacheFilePath: String
    ) {
        this.apiKey = apiKey
        this.projectId = projectId
        this.appId = appId
        this.analyticsApiSecret = analyticsApiSecret
        this.measurementId = measurementId
        this.interval = intervalSeconds

        firebaseCache.init(cacheFilePath)

        withContext(dispatcher) {
            fid = firebaseCache.getFID()
            clientId = firebaseCache.getClientId()
        }
    }

    suspend fun refreshCachedData() {
        firebaseCache.refreshCachedData()

        withContext(dispatcher) {
            fid = firebaseCache.getFID()
            clientId = firebaseCache.getClientId()
        }
    }
}
