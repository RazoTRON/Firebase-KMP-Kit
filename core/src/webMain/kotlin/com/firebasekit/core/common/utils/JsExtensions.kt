package com.firebasekit.core.common.utils

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js

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

fun Map<String, Any?>.toJsAny(): JsAny {
    val jsMap = createPropertyMap()
    this.forEach { (key, value) ->
        addProperty(jsMap, key, value?.toString() ?: "")
    }
    return jsMap
}

fun createPropertyMap(): JsAny = js(
    """({})"""
)

fun addProperty(propertyMap: JsAny, key: String, value: String): Unit = js(
    """{ propertyMap[key] = value; }"""
)
