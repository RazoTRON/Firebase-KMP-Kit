package com.firebasekit.core.common.utils

import com.firebasekit.core.common.models.JsMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.Promise
import kotlin.js.js
import kotlin.js.toJsArray
import kotlin.js.toJsBigInt
import kotlin.js.toJsBoolean
import kotlin.js.toJsNumber
import kotlin.js.toJsReference
import kotlin.js.toJsString

suspend fun <T : JsAny?> Promise<T>.awaitJs(): T =
    suspendCoroutine { cont ->
        then(
            onFulfilled = { value ->
                cont.resume(value)
                null
            },
            onRejected = { error ->
                cont.resumeWithException(Throwable(error?.toString() ?: "Promise rejected"))
                null
            },
        )
    }

fun Map<String, Any>.toJsMap(): JsMap<String, Any> {
    val jsMap = createPropertyMap()
    this.forEach { (key, value) ->
        addProperty(jsMap, key, value.toJsType())
    }
    return JsMap(jsMap)
}

fun Any.toJsType() = when (this) {
    is String -> toJsString()
    is Int -> toJsNumber()
    is Long -> toJsBigInt()
    is Boolean -> toJsBoolean()
    is Collection<*> -> this.toJsCollection()
    else -> toJsReference()
}

fun Collection<Any?>.toJsCollection(): JsArray<JsAny> = map {
    if (it is Collection<*>) { toJsCollection() } else toJsType()
}.toTypedArray().toJsArray()

private fun createPropertyMap(): JsAny = js("""({})""")

private fun addProperty(propertyMap: JsAny, key: String, value: JsAny): Unit = js(
    """{ propertyMap[key] = value; }"""
)

private fun jsonElementToJsValue(json: String): JsAny? = js("JSON.parse(json)")