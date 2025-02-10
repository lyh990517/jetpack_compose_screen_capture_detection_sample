package com.yunho.screencapturedetection

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.yunho.screencapturedetection.LegacyScreenCaptureDetector.Companion.rememberLegacyScreenCaptureDetector
import com.yunho.screencapturedetection.ScreenCaptureDetector.Companion.rememberScreenCaptureDetector

@Composable
fun ScreenCaptureEffect(
    onDetect: () -> Unit,
) {
    val activity = LocalActivity.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            activity.finish()
        }
    }
    val captureDetector = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        rememberScreenCaptureDetector(onDetect)
    } else {
        rememberLegacyScreenCaptureDetector(onDetect)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val filePermission = Manifest.permission.READ_EXTERNAL_STORAGE

            if (!activity.isGranted(filePermission)) {
                launcher.launch(filePermission)
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        captureDetector.registerCallback(activity)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        captureDetector.unregisterCallback(activity)
    }
}

private fun Context.isGranted(permission: String) = ContextCompat.checkSelfPermission(
    this,
    permission
) == PackageManager.PERMISSION_GRANTED

private interface CaptureDetector {
    val onDetect: () -> Unit

    fun registerCallback(activity: Activity)
    fun unregisterCallback(activity: Activity)
}

private class LegacyScreenCaptureDetector(
    override val onDetect: () -> Unit,
) : CaptureDetector {
    private val contentObserver: (Activity) -> ContentObserver = { activity ->
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                uri?.let { checkIfScreenshot(it, activity) }
            }
        }
    }

    override fun registerCallback(activity: Activity) {
        activity.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver(activity)
        )
    }

    override fun unregisterCallback(activity: Activity) {
        activity.contentResolver.unregisterContentObserver(contentObserver(activity))
    }

    private fun checkIfScreenshot(uri: Uri, activity: Activity) {
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        activity.contentResolver.query(uri, projection, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

                    if (pathIndex != -1) {
                        val filePath = cursor.getString(pathIndex)

                        if (filePath.contains("Screenshots", ignoreCase = true)) {
                            onDetect()
                        }
                    }
                }
            }
    }

    companion object {
        @Composable
        fun rememberLegacyScreenCaptureDetector(
            onDetect: () -> Unit,
        ) = remember {
            LegacyScreenCaptureDetector(onDetect)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private class ScreenCaptureDetector(
    override val onDetect: () -> Unit,
) : CaptureDetector {
    private val callback = Activity.ScreenCaptureCallback {
        onDetect()
    }

    override fun registerCallback(activity: Activity) {
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
    }

    override fun unregisterCallback(activity: Activity) {
        activity.unregisterScreenCaptureCallback(callback)
    }

    companion object {
        @Composable
        fun rememberScreenCaptureDetector(
            onDetect: () -> Unit,
        ) = remember {
            ScreenCaptureDetector(onDetect)
        }
    }
}
