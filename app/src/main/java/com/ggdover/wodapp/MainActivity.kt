package com.ggdover.wodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.WodApp
import com.ggdover.wodapp.ui.theme.WodAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WodApplication
        setContent {
            WodAppTheme {
                CompositionLocalProvider(LocalWodRepository provides app.repository) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        WodApp()
                    }
                }
            }
        }
    }
}
