package com.firebasekit.crashlytics

import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.crashlytics.FIRCLSRemoteConfigManager
import swiftPMImport.com.firebasekit.crashlytics.FIRCrashlytics

interface CrashlyticsBridge {
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
    fun didCrashOnPreviousExecution(): Boolean
    fun setUserId(userId: String)
    fun setCustomValue(value: Any, key: String)
    fun setCustomKeysAndValues(keys: Map<Any?, *>)
    fun log(message: String)
    fun recordError(error: NSError)
    fun recordError(error: NSError, userInfo: Map<Any?, *>)
    fun sendUnsentReports()
    fun deleteUnsentReports()
}

class FIRCrashlyticsBridge(
    private val native: FIRCrashlytics = FIRCrashlytics.crashlytics(),
) : CrashlyticsBridge {
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        native.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        return native.didCrashDuringPreviousExecution()
    }

    override fun setUserId(userId: String) {
        native.setUserID(userId)
    }

    override fun setCustomValue(value: Any, key: String) {
        native.setCustomValue(value, key)
    }

    override fun setCustomKeysAndValues(keys: Map<Any?, *>) {
        native.setCustomKeysAndValues(keys)
    }

    override fun log(message: String) {
        native.log(message)
    }

    override fun recordError(error: NSError) {
        native.recordError(error)
    }

    override fun recordError(error: NSError, userInfo: Map<Any?, *>) {
        native.recordError(error, userInfo)
    }

    override fun sendUnsentReports() {
        native.sendUnsentReports()
    }

    override fun deleteUnsentReports() {
        native.deleteUnsentReports()
    }
}
