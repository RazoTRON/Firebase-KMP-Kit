package com.firebasekit.crashlytics

import com.firebasekit.core.Firebase
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics as AndroidFirebaseCrashlytics

actual val Firebase.crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlyticsAndroid() }

class FirebaseCrashlyticsAndroid(
    private val crashlytics: AndroidFirebaseCrashlytics = AndroidFirebaseCrashlytics.getInstance(),
) : FirebaseCrashlytics {
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        return crashlytics.didCrashOnPreviousExecution()
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKeys(keys: CrashlyticsKeys) {
        crashlytics.setCustomKeys(keys.toAndroidCustomKeys())
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun recordException(throwable: Throwable, keys: CrashlyticsKeys) {
        crashlytics.recordException(throwable, keys.toAndroidCustomKeys())
    }

    override fun sendUnsentReports() {
        crashlytics.sendUnsentReports()
    }

    override fun deleteUnsentReports() {
        crashlytics.deleteUnsentReports()
    }

    internal fun CrashlyticsKeys.toAndroidCustomKeys(): CustomKeysAndValues {
        return CustomKeysAndValues.Builder().apply {
            values.forEach { putCrashlyticsKey(it.key, it.value) }
        }.build()
    }

    private fun CustomKeysAndValues.Builder.putCrashlyticsKey(
        key: String,
        value: CrashlyticsKeyValue,
    ) {
        when (value) {
            is CrashlyticsKeyValue.StringValue -> putString(key, value.value)
            is CrashlyticsKeyValue.BooleanValue -> putBoolean(key, value.value)
            is CrashlyticsKeyValue.DoubleValue -> putDouble(key, value.value)
            is CrashlyticsKeyValue.FloatValue -> putFloat(key, value.value)
            is CrashlyticsKeyValue.IntValue -> putInt(key, value.value)
            is CrashlyticsKeyValue.LongValue -> putLong(key, value.value)
        }
    }
}
