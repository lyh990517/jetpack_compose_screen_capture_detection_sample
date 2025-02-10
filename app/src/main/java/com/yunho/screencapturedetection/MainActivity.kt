package com.yunho.screencapturedetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.yunho.screencapturedetection.ui.theme.ScreenCaptureDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalActivity provides this
            ) {
                ScreenCaptureDetectionTheme {
                    TestScreen()
                }
            }
        }
    }
}
