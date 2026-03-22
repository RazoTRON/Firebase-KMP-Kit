package com.firebasekit.sample.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.sample.App

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Firebase.initialize(this)

        setContent { 
            App()
        }
    }
}