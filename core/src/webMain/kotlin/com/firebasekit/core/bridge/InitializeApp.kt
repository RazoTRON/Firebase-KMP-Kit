@file:JsModule("firebase/app")

package com.firebasekit.core.bridge

import kotlin.js.JsAny
import kotlin.js.JsModule

external fun initializeApp(configuration: JsAny): JsAny
