package com.firebasekit.messaging

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class FirebaseMessagingWebTest {

    private fun sut() = FirebaseMessagingWeb()

    @Test
    fun getToken_throwsUnsupportedOperation() = runTest {
        val error = try {
            sut().getToken()
            null
        } catch (error: Throwable) {
            error
        }
        assertNotNull(error)
        assertIs<UnsupportedOperationException>(error)
        assertEquals(UNSUPPORTED_MESSAGE, error.message)
    }

    @Test
    fun subscribeToTopic_throwsUnsupportedOperation() = runTest {
        val error = try {
            sut().subscribeToTopic("news")
            null
        } catch (error: Throwable) {
            error
        }
        assertNotNull(error)
        assertIs<UnsupportedOperationException>(error)
        assertEquals(UNSUPPORTED_MESSAGE, error.message)
    }
}
