package com.truvideo.sdk.media.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileUriUtil {

    fun getMimeType(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "unknown/unknown"
    }

    fun isPhotoOrVideo(context: Context, uri: Uri): Boolean {
        // Get the MIME type of the file from the URI
        val mimeType = getMimeType(context, uri)

        // Check if the file extension is one of the common image or video extensions
        val isImage = mimeType.startsWith("image/")
        val isVideo = mimeType.startsWith("video/")

        // Return true if it's an image or video, false otherwise
        return isImage || isVideo
    }

    fun getExtension(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            var columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (columnIndex < 0) {
                columnIndex = 0
            }
            val displayName = cursor.getString(columnIndex)
            cursor.close()

            displayName?.let {
                val lastDot = it.lastIndexOf(".")
                if (lastDot != -1) {
                    return it.substring(lastDot + 1)
                }
            }
        }

        throw TruvideoSdkException("Invalid file")
    }

    fun createTempFile(context: Context, uri: Uri, fileName: String): File {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: throw TruvideoSdkException("File not found")
            val file = File(context.cacheDir, fileName)
            val outputStream: OutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            return file
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()

            throw TruvideoSdkException("File not found")
        }
    }
}