package com.firebasekit.crashlytics

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.Foundation.NSException
import platform.Foundation.NSNumber
import platform.darwin.NSUInteger
import kotlin.experimental.ExperimentalNativeApi
import kotlin.reflect.KClass

/**
 * Returns a [NSException] representing `this` [Throwable].
 * If [appendCausedBy] is `true` then the name, message and stack trace
 * of the [causes][Throwable.cause] will be appended, else causes are ignored.
 */
@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal fun Throwable.asNSException(appendCausedBy: Boolean = true): NSException {
    val returnAddresses = getFilteredStackTraceAddresses().let { addresses ->
        if (!appendCausedBy) return@let addresses
        addresses.toMutableList().apply {
            for (cause in causes) {
                addAll(cause.getFilteredStackTraceAddresses(true, addresses))
            }
        }
    }.map {
        NSNumber(unsignedInteger = it.convert<NSUInteger>())
    }
    return ThrowableNSException(name, getReason(appendCausedBy), returnAddresses)
}

/**
 * Returns the [qualifiedName][KClass.qualifiedName] or [simpleName][KClass.simpleName] of `this` throwable.
 * If both are `null` then "Throwable" is returned.
 */
internal val Throwable.name: String
    get() = this::class.qualifiedName ?: this::class.simpleName ?: "Throwable"

/**
 * Returns the [message][Throwable.message] of this throwable.
 * If [appendCausedBy] is `true` then caused by lines with the format
 * "Caused by: $[name]: $[message][Throwable.message]" will be appended.
 */
internal fun Throwable.getReason(appendCausedBy: Boolean = false): String? {
    if (!appendCausedBy) return message
    return buildString {
        message?.let(::append)
        for (cause in causes) {
            if (isNotEmpty()) appendLine()
            append("Caused by: ")
            append(cause.name)
            cause.message?.let { append(": $it") }
        }
    }.takeIf { it.isNotEmpty() }
}

internal class ThrowableNSException(
    name: String,
    reason: String?,
    private val returnAddresses: List<NSNumber>
): NSException(name, reason, null) {
    override fun callStackReturnAddresses(): List<NSNumber> = returnAddresses
}
/**
 * Returns a list with all the [causes][Throwable.cause].
 * The first element will be the cause, the second the cause of the cause, etc.
 * This function stops once a reference cycles is detected.
 */
internal val Throwable.causes: List<Throwable>
    get() = buildList {
        val causes = mutableSetOf<Throwable>()
        var cause = cause
        while (cause != null && causes.add(cause)) {
            add(cause)
            cause = cause.cause
        }
    }

/**
 * Returns a list of stack trace addresses representing
 * the stack trace of the constructor call to `this` [Throwable].
 * @param keepLastInit `true` to preserve the last constructor call, `false` to drop all constructor calls.
 * @param commonAddresses a list of addresses used to drop the last common addresses.
 * @see getStackTraceAddresses
 */
@OptIn(ExperimentalNativeApi::class)
internal fun Throwable.getFilteredStackTraceAddresses(
    keepLastInit: Boolean = false,
    commonAddresses: List<Long> = emptyList()
): List<Long> {
    val stackTraceAddresses = getStackTraceAddresses()

    val addressesWithoutInit = stackTraceAddresses.dropInitAddresses(
        qualifiedClassName = this::class.qualifiedName ?: Throwable::class.qualifiedName!!,
        stackTrace = getStackTrace(),
        keepLast = keepLastInit
    )

    val addresses = addressesWithoutInit.dropCommonAddresses(commonAddresses)

    return addresses
}

/**
 * Returns a list containing all addresses except for the first addresses
 * matching the constructor call of the [qualifiedClassName].
 * If [keepLast] is `true` the last constructor call won't be dropped.
 */
internal fun List<Long>.dropInitAddresses(
    qualifiedClassName: String,
    stackTrace: Array<String>,
    keepLast: Boolean = false
): List<Long> {
    val exceptionInit = "kfun:$qualifiedClassName#<init>"
    var dropCount = 0
    var foundInit = false
    for (i in stackTrace.indices) {
        if (stackTrace[i].contains(exceptionInit)) {
            foundInit = true
        } else if (foundInit) {
            dropCount = i
            break
        }
    }
    if (keepLast) dropCount--
    return drop(kotlin.math.max(0, dropCount))
}

/**
 * Returns a list containing all addresses except for the last addresses that match with the [commonAddresses].
 */
internal fun List<Long>.dropCommonAddresses(
    commonAddresses: List<Long>
): List<Long> {
    var i = commonAddresses.size
    if (i == 0) return this
    return dropLastWhile {
        i-- > 0 && commonAddresses[i] == it
    }
}