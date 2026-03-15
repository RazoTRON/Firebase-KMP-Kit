package com.firebasekit.core

import com.firebasekit.core.bridge.initializeApp
import com.firebasekit.core.utils.createConfiguration
import kotlin.js.JsAny

internal var app: JsAny? = null

fun Firebase.initialize(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
    measurementId: String,
) {
    app = initializeApp(
        configuration = createConfiguration(
            apiKey = apiKey,
            authDomain = authDomain,
            projectId = projectId,
            storageBucket = storageBucket,
            messagingSenderId = messagingSenderId,
            appId = appId,
            measurementId = measurementId
        )
    )
}