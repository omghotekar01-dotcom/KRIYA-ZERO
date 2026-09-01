package com.xyro.kriyazero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xyro.kriyazero.ui.KriyaLiveApp
import com.xyro.kriyazero.ui.theme.KriyaZeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KriyaZeroTheme {
                KriyaLiveApp()
            }
        }
    }
}
