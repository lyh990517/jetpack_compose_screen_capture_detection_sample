package com.yunho.screencapturedetection

import android.app.Activity
import androidx.compose.runtime.compositionLocalOf

val LocalActivity = compositionLocalOf<Activity> {
    error("Activity is missing")
}
