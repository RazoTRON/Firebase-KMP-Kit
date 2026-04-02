package com.firebasekit.core

import com.firebasekit.native.FIRApp
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun Firebase.initialize() {
    FIRApp.configure()
}