package com.firebasekit.core

import com.firebasekit.core.bridge.FirebaseApp
import com.firebasekit.core.common.JSBuilder
import com.firebasekit.core.bridge.initializeApp
import kotlin.js.JsAny

var app: FirebaseApp? = null
    private set

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
        options = JSBuilder.build {
            this.apiKey = apiKey
            this.authDomain = authDomain
            this.projectId = projectId
            this.storageBucket = storageBucket
            this.messagingSenderId = messagingSenderId
            this.appId = appId
            this.measurementId = measurementId
        }
    )
}
