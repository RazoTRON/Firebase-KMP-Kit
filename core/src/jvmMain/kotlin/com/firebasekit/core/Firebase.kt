package com.firebasekit.core

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun Firebase.initialize(
    apiKey: String,
    projectId: String,
    appId: String,
    interval: Duration = 60.minutes,
    cacheFilePath: String = "cache/firebase_data"
) {
    FirebaseJvm.initialize(
        apiKey = apiKey,
        projectId = projectId,
        appId = appId,
        intervalSeconds = interval,
        cacheFilePath = cacheFilePath
    )
}

object FirebaseJvm {
    var apiKey: String? = null
        private set
    var projectId: String? = null
        private set
    var appId: String? = null
        private set
    var interval: Duration? = null
        private set
    var fid: String? = null
        private set

    internal fun initialize(
        apiKey: String,
        projectId: String,
        appId: String,
        intervalSeconds: Duration,
        cacheFilePath: String
    ) {
        this.apiKey = apiKey
        this.projectId = projectId
        this.appId = appId
        this.interval = intervalSeconds
        this.fid = FirebaseCache(cacheFile = File(cacheFilePath)).getFID()
    }
}


internal class FirebaseCache(cacheFile: File) {
    private val file = cacheFile

    fun getFID(): String {
        if (file.exists()) {
            return file.readText()
        }

        val fid = generateFid()
        file.parentFile.mkdirs()
        file.writeText(fid)
        return fid
    }

    private fun generateFid(): String {
        val random = ByteArray(17)
        SecureRandom().nextBytes(random)
        random[0] = (random[0].toInt() and 0x0F or 0x70).toByte()

        val encoded = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(random)

        return encoded.take(22)
    }
}
