package com.firebasekit.core.bridge

import kotlin.js.JsAny
import kotlin.js.JsModule

@JsModule("firebase/app")
external interface FirebaseOptions : JsAny {
    var apiKey: String?
    var authDomain: String?
    var databaseURL: String?
    var projectId: String?
    var storageBucket: String?
    var messagingSenderId: String?
    var appId: String?
    var measurementId: String?
}