package com.firebasekit.performance.bridge

import com.firebasekit.core.common.models.JsMap
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
internal fun JsMap<String, String>.toKotlinStringMap(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    copyStringProperties(value) { key, value ->
        attributes[key] = value
    }
    return attributes
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun copyStringProperties(properties: JsAny, add: (String, String) -> Unit): Unit = js(
    """
        {
            var keys = Object.keys(properties);
            for (var i = 0; i < keys.length; i++) {
                var key = keys[i];
                add(key, String(properties[key]));
            }
        }
    """
)

