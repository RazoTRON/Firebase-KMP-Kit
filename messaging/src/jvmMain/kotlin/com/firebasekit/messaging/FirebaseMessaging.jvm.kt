package com.firebasekit.messaging

actual interface FirebaseMessaging {
    actual suspend fun getToken(): String
    actual suspend fun deleteToken()
    suspend fun subscribeToTopic(topic: String)
    suspend fun unsubscribeFromTopic(topic: String)
    fun onMessage(block: (payload: String) -> Unit)
}