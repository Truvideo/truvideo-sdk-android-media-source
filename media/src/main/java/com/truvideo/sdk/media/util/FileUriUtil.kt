package com.truvideo.sdk.media.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

internal object FileUriUtil {

    fun getMimeType(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        var mimeType = contentResolver.getType(uri)

        return if (mimeType != null) {
            mimeType
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
            mimeType ?: "unknown/unknown"
        }
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
        var extension: String? = null
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
                    extension = it.substring(lastDot + 1)
                }
            }
        }
        if (extension != null) {
            return extension as String
        } else {
            val uriSplit = uri.toString().split(".")
            if (uriSplit.isNotEmpty()) {
                return uriSplit.last()
            }
        }

        throw TruvideoSdkException("Invalid file")
    }

    fun realPathFromUri(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        val column = "_data"
        val projection = arrayOf(column)
        val cursor = if (uri != null) context.contentResolver.query(uri, projection, selection, selectionArgs, null) else null
        cursor.use {
            if (it != null && it.moveToFirst()) {
                val index = it.getColumnIndexOrThrow(column)
                val result = it.getString(index)
                it.close()
                return result
            }
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

    private fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority


}