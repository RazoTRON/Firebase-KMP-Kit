@file:JsModule("firebase/app")

package com.firebasekit.core.bridge

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsModule
import kotlin.js.Promise

external interface FirebaseApp : JsAny {
    var name: String
    var options: FirebaseOptions
    var automaticDataCollectionEnabled: Boolean
}

external interface FirebaseAppSettings : JsAny {
    var name: String?
    var automaticDataCollectionEnabled: Boolean?
    var setting: String
}

external fun initializeApp(): FirebaseApp
external fun initializeApp(options: FirebaseOptions): FirebaseApp
external fun initializeApp(options: FirebaseOptions, name: String): FirebaseApp
external fun initializeApp(options: FirebaseOptions, config: FirebaseAppSettings): FirebaseApp

external fun getApp(): FirebaseApp
external fun getApp(name: String): FirebaseApp
external fun getApps(): JsArray<FirebaseApp>

external fun registerVersion(libraryKeyOrName: String, version: String)
external fun registerVersion(libraryKeyOrName: String, version: String, variant: String)

external fun deleteApp(app: FirebaseApp): Promise<JsAny?>

external val SDK_VERSION: String

external fun setLogLevel(logLevel: String /* "debug" | "verbose" | "info" | "warn" | "error" | "silent" */)