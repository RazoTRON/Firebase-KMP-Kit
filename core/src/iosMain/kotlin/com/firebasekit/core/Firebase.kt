package com.firebasekit.core

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.firebasekit.core.FIRApp

@OptIn(ExperimentalForeignApi::class)
fun Firebase.initialize() {
    FIRApp.configure()
}