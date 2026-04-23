package com.firebasekit.analytics

import com.firebasekit.core.Firebase
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

expect val Firebase.analytics: FirebaseAnalytics

interface FirebaseAnalytics {
    fun logEvent(name: String, parameters: Bundle = Bundle())
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun resetAnalyticsData()
}

class Bundle {
    private val values = linkedMapOf<String, BundleValue>()

    fun put(key: String, value: String) {
        values[key] = BundleValue.StringValue(value)
    }

    fun put(key: String, value: Long) {
        values[key] = BundleValue.LongValue(value)
    }

    fun put(key: String, value: Int) {
        put(key, value.toLong())
    }

    fun put(key: String, value: Double) {
        values[key] = BundleValue.DoubleValue(value)
    }

    fun put(key: String, value: Float) {
        put(key, value.toDouble())
    }

    fun put(key: String, value: Boolean) {
        values[key] = BundleValue.BooleanValue(value)
    }

    fun put(key: String, value: JsonElement) {
        values[key] = BundleValue.SerializableValue(value)
    }

    fun <T> put(
        key: String,
        value: T,
        serializer: KSerializer<T>,
    ) {
        put(key, Json.encodeToJsonElement(serializer, value))
    }

    internal fun isEmpty(): Boolean = values.isEmpty()

    internal fun forEach(action: (String, BundleValue) -> Unit) {
        values.forEach { action(it.key, it.value) }
    }
}

internal sealed interface BundleValue {
    @JvmInline
    value class StringValue(val value: String) : BundleValue

    @JvmInline
    value class LongValue(val value: Long) : BundleValue

    @JvmInline
    value class DoubleValue(val value: Double) : BundleValue

    @JvmInline
    value class BooleanValue(val value: Boolean) : BundleValue

    @JvmInline
    value class SerializableValue(val value: JsonElement) : BundleValue
}

internal const val UNSUPPORTED_MESSAGE =
    "Firebase Analytics is only supported on Android and iOS in this module"

internal fun unsupportedAnalytics(): Nothing = throw UnsupportedOperationException(UNSUPPORTED_MESSAGE)
