package com.firebasekit.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

suspend fun Firebase.initialize(
    apiKey: String,
    projectId: String,
    appId: String,
    authDomain: String? = null,
    storageBucket: String? = null,
    messagingSenderId: String? = null,
    webVapidKey: String? = null,
    measurementProtocolApiSecret: String? = null,
    measurementId: String? = null,
    cacheFilePath: String = "cache/firebase_data"
) {
    FirebaseJvm.initialize(
        apiKey = apiKey,
        projectId = projectId,
        appId = appId,
        authDomain = authDomain,
        storageBucket = storageBucket,
        messagingSenderId = messagingSenderId,
        webVapidKey = webVapidKey,
        analyticsApiSecret = measurementProtocolApiSecret,
        measurementId = measurementId,
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
    var authDomain: String? = null
        private set
    var storageBucket: String? = null
        private set
    var messagingSenderId: String? = null
        private set
    var webVapidKey: String? = null
        private set
    var analyticsApiSecret: String? = null
        private set
    var measurementId: String? = null
        private set
    var clientId: String? = null
        private set
    var fid: String? = null

    private val firebaseCache = FirebaseCache()

    internal suspend fun initialize(
        apiKey: String,
        projectId: String,
        appId: String,
        authDomain: String?,
        storageBucket: String?,
        messagingSenderId: String?,
        webVapidKey: String?,
        analyticsApiSecret: String?,
        measurementId: String?,
        cacheFilePath: String
    ) {
        this.apiKey = apiKey
        this.projectId = projectId
        this.appId = appId
        this.authDomain = authDomain
        this.storageBucket = storageBucket
        this.messagingSenderId = messagingSenderId
        this.webVapidKey = webVapidKey
        this.analyticsApiSecret = analyticsApiSecret
        this.measurementId = measurementId

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
