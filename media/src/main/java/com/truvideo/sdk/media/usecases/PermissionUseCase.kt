package com.truvideo.sdk.media.usecases

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

internal class PermissionUseCase {

    private var activity: ComponentActivity? = null
    private var startForResult: ActivityResultLauncher<Array<String>>? = null
    private var completion: CancellableContinuation<Boolean>? = null

    fun init(activity: ComponentActivity) {
        this.activity = activity
        this.startForResult = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val allGranted = it.values.all { permission -> permission }
            completion?.resumeWith(Result.success(allGranted))
        }
    }

    fun hasReadStoragePermission(): Boolean {
        val activity = this.activity ?: throw TruvideoSdkMediaException("Not initialized")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val videoPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
            val imagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
            return videoPermission == PackageManager.PERMISSION_GRANTED && imagePermission == PackageManager.PERMISSION_GRANTED
        } else {
            val storagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            return storagePermission == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun askReadStoragePermission(): Boolean {
        val startForResult = this.startForResult ?: throw TruvideoSdkMediaException("Not initialized")
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        startForResult.launch(permissions.toTypedArray())
        return suspendCancellableCoroutine { completion = it }
    }
}