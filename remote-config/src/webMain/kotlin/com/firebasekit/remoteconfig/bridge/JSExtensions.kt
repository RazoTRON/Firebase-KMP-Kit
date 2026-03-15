package com.firebasekit.remoteconfig.bridge

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsValueAsJsonString(value: JsAny?): String = js(
    """
        value != null 
            ? (typeof value.asString === 'function' 
                ? value.asString() 
                : JSON.stringify(value)) 
            : ""
        """
)

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsValueAsFlattenJsonString(value: JsAny?): String = js(
    """
        (function() {
            if (value == null) return "";
            var result = {};
            var keys = Object.keys(value);
            for (var i = 0; i < keys.length; i++) {
                var k = keys[i];
                var v = value[k];
                result[k] = v != null && typeof v.asString === 'function' ? v.asString() : String(v);
            }
            return JSON.stringify(result);
        })()
        """
)

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsValueAsBoolean(value: JsAny?): Boolean =
    js("value != null && typeof value.asBoolean === 'function' ? value.asBoolean() : false")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsValueAsString(value: JsAny?): String =
    js("value != null && typeof value.asString === 'function' ? value.asString() : ''")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsValueAsNumber(value: JsAny?): Double =
    js("value != null && typeof value.asNumber === 'function' ? value.asNumber() : NaN")