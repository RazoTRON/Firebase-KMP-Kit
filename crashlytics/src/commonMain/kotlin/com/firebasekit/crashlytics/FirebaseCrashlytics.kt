package com.firebasekit.crashlytics

import com.firebasekit.core.Firebase
import kotlin.jvm.JvmInline

expect val Firebase.crashlytics: FirebaseCrashlytics

interface FirebaseCrashlytics {
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
    fun didCrashOnPreviousExecution(): Boolean
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Double)
    fun setCustomKey(key: String, value: Float)
    fun setCustomKey(key: String, value: Int)
    fun setCustomKey(key: String, value: Long)
    fun setCustomKeys(keys: CrashlyticsKeys)
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun recordException(throwable: Throwable, keys: CrashlyticsKeys)
    fun sendUnsentReports()
    fun deleteUnsentReports()
}

class CrashlyticsKeys {
    internal val values = linkedMapOf<String, CrashlyticsKeyValue>()

    fun put(key: String, value: String) {
        values[key] = CrashlyticsKeyValue.StringValue(value)
    }

    fun put(key: String, value: Boolean) {
        values[key] = CrashlyticsKeyValue.BooleanValue(value)
    }

    fun put(key: String, value: Double) {
        values[key] = CrashlyticsKeyValue.DoubleValue(value)
    }

    fun put(key: String, value: Float) {
        values[key] = CrashlyticsKeyValue.FloatValue(value)
    }

    fun put(key: String, value: Int) {
        values[key] = CrashlyticsKeyValue.IntValue(value)
    }

    fun put(key: String, value: Long) {
        values[key] = CrashlyticsKeyValue.LongValue(value)
    }
}

internal sealed interface CrashlyticsKeyValue {
    val value: Any

    @JvmInline
    value class StringValue(override val value: String) : CrashlyticsKeyValue

    @JvmInline
    value class BooleanValue(override val value: Boolean) : CrashlyticsKeyValue

    @JvmInline
    value class DoubleValue(override val value: Double) : CrashlyticsKeyValue

    @JvmInline
    value class FloatValue(override val value: Float) : CrashlyticsKeyValue

    @JvmInline
    value class IntValue(override val value: Int) : CrashlyticsKeyValue

    @JvmInline
    value class LongValue(override val value: Long) : CrashlyticsKeyValue
}

internal const val UNSUPPORTED_MESSAGE =
    "Firebase Crashlytics is only supported on Android in this module"

internal fun unsupportedCrashlytics() {
    if (Firebase.enableLogs) {
        println(UNSUPPORTED_MESSAGE)
    }
}
