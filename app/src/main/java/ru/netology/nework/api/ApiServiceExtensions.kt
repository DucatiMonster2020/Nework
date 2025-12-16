package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.error.AppError
import java.io.File

suspend fun uploadMedia(file: File): MediaResponse {
    val part = MultipartBody.Part.createFormData(
        "file",
        file.name,
        file.asRequestBody()
    )
    val response = uploadMedia(part)
    if (!response.isSuccessful) {
        throw AppError(response.code(), response.message())
    }
    return response.body() ?: throw AppError(response.code(), response.message())
}
suspend fun <T>
        ApiService.executeCall(call: suspend () ->
Response<T>): T {
    val response = call()
    if (!response.isSuccessful) {
        throw AppError(response.code(), response.message())
    }
    return response.body() ?: throw AppError(response.code(), response.message())
}
