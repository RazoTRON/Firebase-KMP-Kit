package com.firebasekit.core.utils

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js

fun createConfiguration(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
    measurementId: String,
): JsAny = js(
    """
    ({
        apiKey: apiKey,
        authDomain: authDomain,
        projectId: projectId,
        storageBucket: storageBucket,
        messagingSenderId: messagingSenderId,
        appId: appId,
        measurementId: measurementId
    })
"""
)

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