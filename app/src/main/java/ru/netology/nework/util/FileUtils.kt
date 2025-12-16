package ru.netology.nework.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Pictures")

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    fun createTempFile(context: Context, prefix: String, extension: String): File {
        val cacheDir = context.cacheDir
        return File.createTempFile(prefix, ".$extension", cacheDir)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun isCameraAvailable(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            }
            ContentResolver.SCHEME_FILE -> {
                uri.lastPathSegment
            }
            else -> null
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.getType(uri)
            }
            ContentResolver.SCHEME_FILE -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            }
            else -> null
        }
    }

    fun copyFileToCache(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val cacheFile = createTempFile(context, "cache_", "tmp")

            FileOutputStream(cacheFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()

            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun formatFileSize(size: Long): String {
        return when {
            size >= 1024 * 1024 -> "${String.format("%.1f", size / (1024.0 * 1024.0))} МБ"
            size >= 1024 -> "${String.format("%.1f", size / 1024.0)} КБ"
            else -> "$size Б"
        }
    }

    fun getMaxFileSizeBytes(): Long = 15 * 1024 * 1024 // 15 МБ

    fun isFileSizeValid(fileSize: Long): Boolean {
        val maxSizeBytes = 15 * 1024 * 1024 // 15 МБ
        return fileSize <= maxSizeBytes
    }
    fun checkFileSize(context: Context, uri: Uri): Boolean {
        val fileSize = getFileSize(context, uri)
        return isFileSizeValid(fileSize)
    }
    fun getFileSizeErrorMessage(context: Context, uri: Uri): String? {
        val fileSize = getFileSize(context, uri)
        val maxSizeMB = 15
        val fileSizeMB = fileSize / (1024 * 1024)
        return if (fileSize > maxSizeMB * 1024 * 1024) {
            "Файл слишком большой: ${fileSizeMB} МБ. Максимальный размер: $maxSizeMB МБ"
        } else {
            null
        }
    }
}