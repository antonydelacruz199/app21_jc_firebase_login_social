package com.example.app21_jc_firebase_login_social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app21_jc_firebase_login_social.ui.theme.App21_jc_firebase_login_socialTheme
import com.example.app21_jc_firebase_login_social.view.NavegacionApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Revisa el nombre real del tema en ui/theme/Theme.kt
            App21_jc_firebase_login_socialTheme {
                NavegacionApp()
            }
        }
    }
}