@file:JsModule("firebase/remote-config")

package com.firebasekit.remoteconfig.bridge

import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.Promise

external fun getRemoteConfig(app: JsAny): JsAny
external fun fetchAndActivate(remoteConfig: JsAny): Promise<JsAny>
external fun getValue(remoteConfig: JsAny, key: String): JsAny?
external fun getAll(remoteConfig: JsAny): JsAny?