package ru.netology.nework.repository

import android.media.session.MediaSession
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.AuthenticationResponse
import ru.netology.nework.dto.LoginRequest
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.RegisterRequest
import ru.netology.nework.error.asAppError
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(login: String, password: String): Token {
        try {
            val response = apiService.loginUser(login, password)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> throw ApiError(400, "Неправильный логин или пароль")
                    else -> throw ApiError(response.code(), response.message())
                }
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun register(
        login: RequestBody,
        password: RequestBody,
        name: RequestBody,
        avatar: MultipartBody.Part?
    ): Token {
        try {
            val response = apiService.registerUser(login, password, name, avatar)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> throw ApiError(400, "Пользователь с таким логином уже зарегистрирован")
                    else -> throw ApiError(response.code(), response.message())
                }
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun logout() {
        try {
            val response = apiService.logoutUser()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}