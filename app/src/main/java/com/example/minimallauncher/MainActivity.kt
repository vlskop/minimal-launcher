package com.example.minimallauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.minimallauncher.launcher.home.HomeScreen
import com.example.minimallauncher.ui.theme.MinimalLauncherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MinimalLauncherTheme {
                HomeScreen()
            }
        }
    }
}