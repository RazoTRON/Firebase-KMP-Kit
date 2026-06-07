package com.firebasekit.crashlytics

import com.firebasekit.core.Firebase
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.experimental.ExperimentalNativeApi

actual val Firebase.crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlyticsIos() }

class FirebaseCrashlyticsIos(
    private val crashlytics: CrashlyticsBridge = FIRCrashlyticsBridge(),
) : FirebaseCrashlytics {

    @OptIn(ExperimentalNativeApi::class)
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)

        setUnhandledExceptionHook {
            Firebase.crashlytics.recordException(it)
            terminateWithUnhandledException(it)
        }
    }

    override fun didCrashOnPreviousExecution(): Boolean {
        return crashlytics.didCrashOnPreviousExecution()
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomValue(value, key)
    }

    override fun setCustomKeys(keys: CrashlyticsKeys) {
        crashlytics.setCustomKeysAndValues(keys.toNativeUserInfo())
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        println("SSSSS Error: ${throwable.asNSException()}")
//        tryFIRCLSExceptionRecordNSException(throwable.asNSException())
//        crashlytics.recordError(throwable.toNSError())
    }

    override fun recordException(throwable: Throwable, keys: CrashlyticsKeys) {
        crashlytics.recordError(throwable.toNSError(), keys.toNativeUserInfo())
    }

    override fun sendUnsentReports() {
        crashlytics.sendUnsentReports()
    }

    override fun deleteUnsentReports() {
        crashlytics.deleteUnsentReports()
    }
}

private fun CrashlyticsKeys.toNativeUserInfo(): Map<Any?, *> {
    return buildMap {
        this@toNativeUserInfo.values.forEach { put(it.key, it.value.toNativeValue()) }
    }
}

private fun CrashlyticsKeyValue.toNativeValue(): Any = when (this) {
    is CrashlyticsKeyValue.StringValue -> value
    is CrashlyticsKeyValue.BooleanValue -> value
    is CrashlyticsKeyValue.DoubleValue -> value
    is CrashlyticsKeyValue.FloatValue -> value
    is CrashlyticsKeyValue.IntValue -> value
    is CrashlyticsKeyValue.LongValue -> value
}

private fun Throwable.toNSError(): NSError {
    val message = message ?: toString()
    return NSError.errorWithDomain(
        domain = this::class.simpleName.orEmpty(),
        code = 0L,
        userInfo = mapOf(NSLocalizedDescriptionKey to message),
    )
}

private const val FIREBASE_KIT_CRASHLYTICS_ERROR_DOMAIN =
    "com.firebasekit.crashlytics.KotlinThrowable"
