package com.firebasekit.messaging

internal data class DesktopMessagingConfig(
    val apiKey: String,
    val authDomain: String,
    val projectId: String,
    val storageBucket: String?,
    val messagingSenderId: String,
    val appId: String,
    val measurementId: String?,
    val webVapidKey: String,
)