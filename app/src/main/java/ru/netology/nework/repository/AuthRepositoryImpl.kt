package ru.netology.nework.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.AuthenticationResponse
import ru.netology.nework.dto.LoginRequest
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.RegisterRequest
import ru.netology.nework.error.asAppError
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun login(login: String, password: String): AuthenticationResponse {
        try {
            return apiService.login(LoginRequest(login, password))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun register(
        login: String,
        password: String,
        name: String,
        avatar: File?
    ): AuthenticationResponse {
        try {
            val avatarUpload = avatar?.let { uploadAvatar(it) }
            val request = RegisterRequest(
                login = login,
                pass = password,
                name = name,
                file = avatarUpload?.let { MediaUpload(it.id) }
            )
            return apiService.register(request)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun uploadAvatar(file: File): MediaResponse {
        try {
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            return apiService.uploadMedia(part)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }
}