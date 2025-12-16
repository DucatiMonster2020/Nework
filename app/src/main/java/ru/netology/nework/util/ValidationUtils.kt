package ru.netology.nework.util

import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import ru.netology.nework.R
import ru.netology.nework.dto.Event
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object ValidationUtils {

    fun validateLogin(login: String): ValidationResult {
        return when {
            login.isBlank() -> ValidationResult.Error(R.string.error_login_empty)
            login.length < 3 -> ValidationResult.Error(R.string.error_login_too_short)
            login.length > 50 -> ValidationResult.Error(R.string.error_login_too_long)
            !login.matches(Regex("^[a-zA-Z0-9_.-]+$")) ->
                ValidationResult.Error(R.string.error_login_invalid_chars)

            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.error_name_empty)
            name.length < 2 -> ValidationResult.Error(R.string.error_name_too_short)
            name.length > 100 -> ValidationResult.Error(R.string.error_name_too_long)
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(R.string.error_password_empty)
            password.length < 6 -> ValidationResult.Error(R.string.error_password_too_short)
            password.length > 50 -> ValidationResult.Error(R.string.error_password_too_long)
            !password.any { it.isDigit() } ->
                ValidationResult.Error(R.string.error_password_no_digit)

            !password.any { it.isLetter() } ->
                ValidationResult.Error(R.string.error_password_no_letter)

            else -> ValidationResult.Success
        }
    }

    fun validatePasswordConfirmation(
        password: String,
        confirmPassword: String
    ): ValidationResult {
        return if (password != confirmPassword) {
            ValidationResult.Error(R.string.error_password_mismatch)
        } else {
            ValidationResult.Success
        }
    }

    fun validateAvatar(filePath: String?): ValidationResult {
        if (filePath.isNullOrBlank()) return ValidationResult.Success

        val file = File(filePath)
        if (!file.exists()) {
            return ValidationResult.Error(R.string.error_file_not_found)
        }

        val fileSizeInMb = file.length() / (1024 * 1024)
        if (fileSizeInMb > 5) { // 5 МБ максимум
            return ValidationResult.Error(R.string.error_file_too_large)
        }


        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension)
        val allowedTypes = listOf("image/jpeg", "image/png", "image/jpg")

        if (mimeType !in allowedTypes) {
            return ValidationResult.Error(R.string.error_invalid_image_format)
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)

        val maxDimension = 2048
        if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
            return ValidationResult.Error(R.string.error_image_dimensions_too_large)
        }

        return ValidationResult.Success
    }

    fun validateMediaFile(filePath: String?, maxSizeMb: Int = 15): ValidationResult {
        if (filePath.isNullOrBlank()) return ValidationResult.Success

        val file = File(filePath)
        if (!file.exists()) {
            return ValidationResult.Error(R.string.error_file_not_found)
        }

        val fileSizeInMb = file.length() / (1024 * 1024)
        if (fileSizeInMb > maxSizeMb) {
            return ValidationResult.Error(R.string.error_media_file_too_large)
        }

        return ValidationResult.Success
    }

    fun validateDateTime(dateTimeStr: String): ValidationResult {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val dateTime = LocalDateTime.parse(dateTimeStr, formatter)

            if (dateTime.isBefore(LocalDateTime.now())) {
                ValidationResult.Error(R.string.error_date_in_past)
            } else {
                ValidationResult.Success
            }
        } catch (e: DateTimeParseException) {
            ValidationResult.Error(R.string.error_invalid_date_format)
        }
    }

    fun formatDateTimeForDisplay(dateTimeStr: String): String {
        return try {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val parsedDateTime = LocalDateTime.parse(dateTimeStr, formatter)
            parsedDateTime.format(outputFormatter)
        } catch (e: Exception) {
            dateTimeStr
        }
    }
    fun formatEventDateTime(event: Event): String {
        return formatDateTimeForDisplay(event.datetime)
    }
    fun formatPublishedDateTime(publishedStr: String): String {
        return formatDateTimeForDisplay(publishedStr)
    }
    fun formatDateTime(dateTime: String?): String {
        if (dateTime.isNullOrBlank()) return ""

        return try {
            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

            val parsedDateTime = LocalDateTime.parse(dateTime, inputFormatter)
            parsedDateTime.format(outputFormatter)
        } catch (e: Exception) {
            dateTime
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val messageResId: Int) : ValidationResult()
    }
}