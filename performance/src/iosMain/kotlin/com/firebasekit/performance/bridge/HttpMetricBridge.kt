package com.firebasekit.performance.bridge

import swiftPMImport.com.firebasekit.performance.FIRHTTPMetric

interface HttpMetricBridge {
    fun start()
    fun stop()
    fun setRequestPayloadSize(bytes: Long)
    fun setResponsePayloadSize(bytes: Long)
    fun setHttpResponseCode(code: Int)
    fun setResponseContentType(contentType: String?)
    fun putAttribute(name: String, value: String)
    fun getAttribute(name: String): String?
    fun getAttributes(): Map<String, String>
    fun removeAttribute(name: String)
}

class FIRHttpMetricBridge(
    private val native: FIRHTTPMetric,
) : HttpMetricBridge {
    override fun start() {
        native.start()
    }

    override fun stop() {
        native.stop()
    }

    override fun setRequestPayloadSize(bytes: Long) {
        native.requestPayloadSize = bytes
    }

    override fun setResponsePayloadSize(bytes: Long) {
        native.responsePayloadSize = bytes
    }

    override fun setHttpResponseCode(code: Int) {
        native.responseCode = code.toLong()
    }

    override fun setResponseContentType(contentType: String?) {
        native.responseContentType = contentType
    }

    override fun putAttribute(name: String, value: String) {
        native.setValue(value, forAttribute = name)
    }

    override fun getAttribute(name: String): String? {
        return native.valueForAttribute(name)
    }

    override fun getAttributes(): Map<String, String> {
        return native.attributes.toStringMap()
    }

    override fun removeAttribute(name: String) {
        native.removeAttribute(name)
    }
}

private fun Map<Any?, *>.toStringMap(): Map<String, String> =
    entries.associate { it.key.toString() to it.value.toString() }

