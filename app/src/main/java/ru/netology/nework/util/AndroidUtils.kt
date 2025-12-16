package ru.netology.nework.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import ru.netology.nework.R
import java.io.File
import java.text.DecimalFormat

object AndroidUtils {
    private const val AUTHORITY = "ru.netology.nework.fileprovider"

    fun showPopupMenu(
        anchor: View,
        menuRes: Int,
        onEditClick: (() -> Unit)? = null,
        onDeleteClick: (() -> Unit)? = null
    ) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(menuRes)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> {
                        onEditClick?.invoke()
                        true
                    }
                    R.id.delete -> {
                        onDeleteClick?.invoke()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> "${count / 1_000_000}M"
            count >= 1_000 -> "${count / 1_000}K"
            else -> count.toString()
        }
    }

    fun getFilePathFromUri(uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                null
            }
            ContentResolver.SCHEME_FILE -> {
                uri.path
            }
            else -> null
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
            }
            else -> uri.lastPathSegment
        }
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    cursor.getLong(sizeIndex)
                }
            }
            else -> File(uri.path ?: return null).length()
        }
    }

    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }

    fun shareContent(context: Context, text: String, subject: String? = null) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    fun getTempFile(context: Context, prefix: String, extension: String): File {
        val cacheDir = context.cacheDir
        return File.createTempFile(prefix, ".$extension", cacheDir)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, AUTHORITY, file)
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().dispalyMetrics.density).toInt()
    }
    fun getString(@StringRes resId: Int): String {
        return Resouces.getSystem().getString(resId)
    }
}