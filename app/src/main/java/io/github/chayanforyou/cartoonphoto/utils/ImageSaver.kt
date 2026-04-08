package io.github.chayanforyou.cartoonphoto.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object ImageSaver {

    /**
     * Saves a bitmap to the device gallery
     * @param context Application context
     * @param bitmap The bitmap to save
     * @param displayName The name of the file (without extension)
     * @param mimeType The MIME type (default: "image/jpeg")
     * @param quality Compression quality (0-100, default: 90)
     * @return Result indicating success or failure with message
     */
    fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        displayName: String = "Cartoon_${System.currentTimeMillis()}",
        mimeType: String = "image/jpeg",
        quality: Int = 90
    ): SaveResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageAndroidQ(context, bitmap, displayName, mimeType, quality)
            } else {
                saveImageLegacy(context, bitmap, displayName, mimeType, quality)
            }
        } catch (e: Exception) {
            SaveResult.Error(e.message ?: "Failed to save image")
        }
    }

    private fun saveImageAndroidQ(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        mimeType: String,
        quality: Int
    ): SaveResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CartoonPhoto")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return SaveResult.Error("Failed to create media store entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            val format = if (mimeType == "image/png") {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, quality, outputStream)
        } ?: return SaveResult.Error("Failed to open output stream")

        return SaveResult.Success("Image saved to gallery")
    }

    @Suppress("DEPRECATION")
    private fun saveImageLegacy(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        mimeType: String,
        quality: Int
    ): SaveResult {
        val imagesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + "/CartoonPhoto"

        val dir = File(imagesDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val extension = if (mimeType == "image/png") "png" else "jpg"
        val file = File(dir, "$displayName.$extension")

        FileOutputStream(file).use { outputStream ->
            val format = if (mimeType == "image/png") {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, quality, outputStream)
        }

        // Notify gallery
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf(mimeType),
            null
        )

        return SaveResult.Success("Image saved to gallery")
    }

    sealed class SaveResult {
        data class Success(val message: String) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
}
