package com.firebasekit.remoteconfig

import com.firebasekit.core.Firebase
import com.firebasekit.native.FIRRemoteConfigFetchAndActivateStatus
import com.firebasekit.native.FIRRemoteConfigFetchStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfigIos()

@OptIn(ExperimentalForeignApi::class)
class FirebaseRemoteConfigIos(private val remoteConfig: RemoteConfig = FIRRemoteConfigBridge()) : FirebaseRemoteConfig {
    override suspend fun fetchAndActivate() {
        suspendCancellableCoroutine { cont ->
            remoteConfig.fetchAndActivateWithCompletionHandler { status, error ->
                if (status == FIRRemoteConfigFetchAndActivateStatus.FIRRemoteConfigFetchAndActivateStatusSuccessFetchedFromRemote) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(Exception(error?.localizedDescription ?: "fetchAndActivate failed"))
                }
            }
        }
    }

    override fun getBoolean(key: String): Boolean = remoteConfig.configValueForKey(key).boolValue
    override fun getString(key: String): String = remoteConfig.configValueForKey(key).stringValue
    override fun getLong(key: String): Long = remoteConfig.configValueForKey(key).longValue
    override fun getInt(key: String): Int = remoteConfig.configValueForKey(key).intValue
    override fun getDouble(key: String): Double? = remoteConfig.configValueForKey(key).doubleValue
        .takeIf { it.isFinite() }

    @Suppress("UNCHECKED_CAST")
    override fun allToJson(): String? = runCatching {
        val keys = remoteConfig.keysWithPrefix("") as Set<String>
        val entries = keys.associateBy { key -> remoteConfig.configValueForKey(key).stringValue }
        Json.encodeToString(entries)
    }.getOrNull()

    fun getLastFetchTime() = remoteConfig.lastFetchTime?.toKotlinInstant()
    fun getLastFetchStatus() = when (remoteConfig.lastFetchStatus) {
        FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusSuccess -> "SUCCESS"
        FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusFailure -> "FAILURE"
        FIRRemoteConfigFetchStatus.FIRRemoteConfigFetchStatusThrottled -> "THROTTLED"
        else -> "UNKNOWN"
    }

    fun getMinimumFetchInterval() = remoteConfig.configSettings.minimumFetchInterval
        .toDuration(DurationUnit.SECONDS).inWholeSeconds
}
