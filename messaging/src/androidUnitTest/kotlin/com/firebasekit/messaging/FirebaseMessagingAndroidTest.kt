package com.firebasekit.messaging

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging as AndroidFirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FirebaseMessagingAndroidTest {

    private val mockInstance: AndroidFirebaseMessaging = mockk()

    private fun sut() = FirebaseMessagingAndroid(mockInstance)

    private fun tokenTask(value: String): Task<String> = mockk<Task<String>>().also { task ->
        every { task.isSuccessful } returns true
        every { task.result } returns value
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<String>>().onComplete(task)
            task
        }
    }

    private fun voidTask(): Task<Void> = mockk<Task<Void>>().also { task ->
        every { task.isSuccessful } returns true
        every { task.result } returns null
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(task)
            task
        }
    }

    private fun <T> failureTask(cause: Exception): Task<T> = mockk<Task<T>>().also { task ->
        every { task.isSuccessful } returns false
        every { task.exception } returns cause
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<T>>().onComplete(task)
            task
        }
    }

    @Test
    fun getToken_returnsRegistrationToken_whenTaskSucceeds() = runTest {
        every { mockInstance.token } returns tokenTask("fcm-token")

        assertEquals("fcm-token", sut().getToken())
        verify(exactly = 1) { mockInstance.token }
    }

    @Test
    fun getToken_throws_whenTaskFails() = runTest {
        val cause = RuntimeException("token failed")
        every { mockInstance.token } returns failureTask(cause)

        val error = assertFailsWith<RuntimeException> { sut().getToken() }
        assertEquals("token failed", error.message)
    }

    @Test
    fun deleteToken_completes_whenTaskSucceeds() = runTest {
        every { mockInstance.deleteToken() } returns voidTask()

        sut().deleteToken()
        verify(exactly = 1) { mockInstance.deleteToken() }
    }

    @Test
    fun deleteToken_throws_whenTaskFails() = runTest {
        val cause = RuntimeException("delete failed")
        every { mockInstance.deleteToken() } returns failureTask(cause)

        val error = assertFailsWith<RuntimeException> { sut().deleteToken() }
        assertEquals("delete failed", error.message)
    }

    @Test
    fun subscribeToTopic_delegatesToFirebaseMessaging() = runTest {
        every { mockInstance.subscribeToTopic("news") } returns voidTask()

        sut().subscribeToTopic("news")
        verify(exactly = 1) { mockInstance.subscribeToTopic("news") }
    }

    @Test
    fun unsubscribeFromTopic_delegatesToFirebaseMessaging() = runTest {
        every { mockInstance.unsubscribeFromTopic("news") } returns voidTask()

        sut().unsubscribeFromTopic("news")
        verify(exactly = 1) { mockInstance.unsubscribeFromTopic("news") }
    }
}
