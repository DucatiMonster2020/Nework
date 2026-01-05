package ru.netology.nework.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val jobDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatDateTime(dateString: String, pattern: String = "dd.MM.yyyy HH:mm"): String {
        return try {
            val date = isoFormatter.parse(dateString) ?: return dateString
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDate(dateString: String, pattern: String = "dd MMM yyyy"): String {
        return try {
            val date = isoFormatter.parse(dateString) ?: return dateString
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatToApiDate(date: Date): String {
        return isoFormatter.format(date)
    }
}
fun String.formatDateTime(pattern: String = "dd.MM.yyyy HH:mm") =
    DateUtils.formatDateTime(this, pattern)

fun String.formatDate(pattern: String = "dd MMM yyyy") =
    DateUtils.formatDate(this, pattern)