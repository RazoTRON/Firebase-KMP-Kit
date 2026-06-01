package com.firebasekit.remoteconfig

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSError
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfig
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigFetchAndActivateStatus
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigFetchStatus
import swiftPMImport.com.firebasekit.remote.config.FIRRemoteConfigSettings

@OptIn(ExperimentalForeignApi::class)
interface IosRemoteConfig {
    val configSettings: Settings
    val lastFetchStatus: FIRRemoteConfigFetchStatus
    val lastFetchTime: NSDate?

    fun keysWithPrefix(prefix: String): Set<*>
    fun configValueForKey(key: String): Value
    fun fetchAndActivateWithCompletionHandler(
        completionHandler: ((FIRRemoteConfigFetchAndActivateStatus, NSError?) -> Unit)?
    )

    interface Settings {
        val minimumFetchInterval: Double
    }

    interface Value {
        val boolValue: Boolean
        val stringValue: String
        val longValue: Long
        val intValue: Int
        val doubleValue: Double
    }
}

@OptIn(ExperimentalForeignApi::class)
class FIRRemoteConfigBridge(
    private val native: FIRRemoteConfig = FIRRemoteConfig.remoteConfig()
) : IosRemoteConfig {

    override val configSettings: IosRemoteConfig.Settings
        get() = SettingsAdapter(native.configSettings)

    override val lastFetchStatus: FIRRemoteConfigFetchStatus
        get() = native.lastFetchStatus

    override val lastFetchTime: NSDate?
        get() = native.lastFetchTime

    override fun keysWithPrefix(prefix: String): Set<*> = native.keysWithPrefix(prefix)

    override fun configValueForKey(key: String): IosRemoteConfig.Value = ValueAdapter(native, key)

    override fun fetchAndActivateWithCompletionHandler(
        completionHandler: ((FIRRemoteConfigFetchAndActivateStatus, NSError?) -> Unit)?
    ) = native.fetchAndActivateWithCompletionHandler(completionHandler)

    private class SettingsAdapter(
        private val native: FIRRemoteConfigSettings
    ) : IosRemoteConfig.Settings {
        override val minimumFetchInterval: Double
            get() = native.minimumFetchInterval
    }

    private class ValueAdapter(
        private val native: FIRRemoteConfig,
        private val key: String,
    ) : IosRemoteConfig.Value {
        override val boolValue: Boolean get() = native.configValueForKey(key).boolValue
        override val stringValue: String get() = native.configValueForKey(key).stringValue
        override val longValue: Long get() = native.configValueForKey(key).numberValue.longValue
        override val intValue: Int get() = native.configValueForKey(key).numberValue.intValue
        override val doubleValue: Double get() = native.configValueForKey(key).numberValue.doubleValue
    }
}
