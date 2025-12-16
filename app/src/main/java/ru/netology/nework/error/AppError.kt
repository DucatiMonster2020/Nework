package ru.netology.nework.error

import retrofit2.HttpException
import java.io.IOException

sealed class AppError : Exception() {
    data class ApiError(
        val code: Int,
        override val message: String?
    ) : AppError()
    data class NetworkError(val error: IOException) : AppError()
    data class UnknownError(val error: Throwable) : AppError()
    data class ValidationError(
        val field: String,
        override val message: String
    ) : AppError()
}
fun Throwable.asAppError(): AppError = when (this) {
    is IOException -> AppError.NetworkError(this)
    is AppError -> this
    is HttpException -> {
        val errorMessage = try {
            response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        AppError.ApiError(code(), errorMessage ?: message())
    }
    else -> AppError.UnknownError(this)
}