package com.firebasekit.core

import android.content.Context
import com.google.firebase.FirebaseApp

fun Firebase.initialize(context: Context) {
    FirebaseApp.initializeApp(context)
}
