package com.firebasekit.core.common

import kotlin.js.JsAny
import kotlin.js.js

interface JSBuilder {
    companion object {
        fun <T : JsAny> build(block: T.() -> Unit) = createJsObject<T>().apply(block)
    }
}

private fun <T : JsAny> createJsObject(): T = js("({})")