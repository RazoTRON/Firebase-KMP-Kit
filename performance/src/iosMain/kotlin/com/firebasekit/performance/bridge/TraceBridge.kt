package com.firebasekit.performance.bridge

import swiftPMImport.com.firebasekit.performance.FIRTrace

interface TraceBridge {
    fun start()
    fun stop()
    fun incrementMetric(name: String, by: Long)
    fun putMetric(name: String, value: Long)
    fun getMetric(name: String): Long
    fun putAttribute(name: String, value: String)
    fun getAttribute(name: String): String?
    fun getAttributes(): Map<String, String>
    fun removeAttribute(name: String)
}

class FIRTraceBridge(
    private val native: FIRTrace,
) : TraceBridge {
    override fun start() {
        native.start()
    }

    override fun stop() {
        native.stop()
    }

    override fun incrementMetric(name: String, by: Long) {
        native.incrementMetric(name, byInt = by)
    }

    override fun putMetric(name: String, value: Long) {
        native.setIntValue(value, forMetric = name)
    }

    override fun getMetric(name: String): Long {
        return native.valueForIntMetric(name)
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
