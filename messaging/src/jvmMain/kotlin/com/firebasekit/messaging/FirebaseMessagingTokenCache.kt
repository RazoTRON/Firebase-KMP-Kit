package com.firebasekit.messaging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal class FirebaseMessagingTokenCache(
    private val cacheFile: () -> File,
    private val refreshDuration: () -> Duration,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val nowEpochSeconds: Long get() = Clock.System.now().epochSeconds

    private val json = Json { ignoreUnknownKeys = true }

    init {
        CoroutineScope(dispatcher).launch {
            updateExpirationDate(refreshDuration = refreshDuration())
        }
    }

    suspend fun getValidToken(): String? = withContext(dispatcher) {
        readCachedToken()
            ?.takeIf { it.isValid(nowEpochSeconds) }
            ?.token
    }

    suspend fun save(token: String) = withContext(dispatcher) {
        val cacheFile = cacheFile()
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeText(
            json.encodeToString(
                CachedToken(
                    token = token,
                    savedAtEpochSeconds = nowEpochSeconds,
                    refreshDurationSeconds = refreshDuration().inWholeSeconds,
                )
            )
        )
    }

    suspend fun clear() = withContext(dispatcher) {
        val cacheFile = cacheFile()
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    private fun readCachedToken(): CachedToken? {
        val cacheFile = cacheFile()
        if (cacheFile.exists().not()) return null

        return runCatching { json.decodeFromString<CachedToken>(cacheFile.readText()) }
            .getOrNull()
    }

    private suspend fun updateExpirationDate(refreshDuration: Duration) = withContext(dispatcher) {
        readCachedToken()?.let {
            val cacheFile = cacheFile()
            cacheFile.writeText(
                json.encodeToString(it.copy(refreshDurationSeconds = refreshDuration.inWholeSeconds))
            )
        }
    }

    private fun CachedToken.isValid(now: Long): Boolean {
        if (token.isBlank() || refreshDurationSeconds <= 0) return false

        return now < savedAtEpochSeconds + refreshDurationSeconds
    }

    @Serializable
    private data class CachedToken(
        val token: String,
        val savedAtEpochSeconds: Long,
        val refreshDurationSeconds: Long,
    )
}
