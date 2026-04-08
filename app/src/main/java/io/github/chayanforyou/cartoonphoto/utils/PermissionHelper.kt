package io.github.chayanforyou.cartoonphoto.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    /**
     * Checks if storage permission is needed based on Android version
     */
    fun isStoragePermissionNeeded(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    /**
     * Checks if storage permission is granted
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No permission needed for Android 10+
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Gets the storage permission string
     */
    fun getStoragePermission(): String {
        return Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
