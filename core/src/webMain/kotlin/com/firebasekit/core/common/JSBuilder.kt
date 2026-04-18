package com.firebasekit.core.common

import kotlin.js.js

interface JSBuilder {
    companion object {
        fun <T> build(block: T.() -> Unit) = createJsObject<T>().apply(block)
    }
}

private fun <T> createJsObject(): T = js("({})")