package com.yunho.screencapturedetection

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TestScreen() {
    val context = LocalContext.current

    ScreenCaptureEffect {
        Toast.makeText(context, "screen capture detected!!", Toast.LENGTH_SHORT).show()
    }
}
