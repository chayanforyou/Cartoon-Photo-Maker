package io.github.chayanforyou.cartoonphoto

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import io.github.chayanforyou.cartoonphoto.ui.screens.HomeScreen
import io.github.chayanforyou.cartoonphoto.ui.screens.ResultScreen
import io.github.chayanforyou.cartoonphoto.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set status bar icons to dark (black)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        
        setContent {
            AppTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                
                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            onNavigateToResult = { original, cartoon ->
                                currentScreen = Screen.Result(original, cartoon)
                            }
                        )
                    }
                    is Screen.Result -> {
                        ResultScreen(
                            originalBitmap = screen.originalBitmap,
                            cartoonBitmap = screen.cartoonBitmap,
                            onNavigateBack = {
                                currentScreen = Screen.Home
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class Result(val originalBitmap: Bitmap, val cartoonBitmap: Bitmap) : Screen()
}