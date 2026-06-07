package com.firebasekit.crashlytics

import com.firebasekit.core.Firebase

actual val Firebase.crashlytics: FirebaseCrashlytics by lazy { UnsupportedFirebaseCrashlytics() }

private class UnsupportedFirebaseCrashlytics : FirebaseCrashlytics {
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) = unsupportedCrashlytics()

    override fun didCrashOnPreviousExecution(): Boolean {
        unsupportedCrashlytics()
        return false
    }

    override fun setUserId(userId: String) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: String) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: Boolean) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: Double) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: Float) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: Int) = unsupportedCrashlytics()
    override fun setCustomKey(key: String, value: Long) = unsupportedCrashlytics()
    override fun setCustomKeys(keys: CrashlyticsKeys) = unsupportedCrashlytics()
    override fun log(message: String) = unsupportedCrashlytics()
    override fun recordException(throwable: Throwable) = unsupportedCrashlytics()
    override fun recordException(throwable: Throwable, keys: CrashlyticsKeys) = unsupportedCrashlytics()
    override fun sendUnsentReports() = unsupportedCrashlytics()
    override fun deleteUnsentReports() = unsupportedCrashlytics()
}
