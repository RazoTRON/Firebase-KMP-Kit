package com.firebasekit.sample.common

enum class Platform {
    ANDROID,
    IOS,
    WEB,
    DESKTOP;

    companion object
}

expect fun Platform.Companion.current(): Platform