package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Token
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(login: String, password: String): Token {
        val response = apiService.login(
            ApiService.AuthRequest(login, password)
        )

        if (!response.isSuccessful) {
            throw ApiError("Ошибка авторизации", response.code())
        }

        return response.body() ?: throw ApiError("Пустой ответ от сервера")
    }

    override suspend fun register(login: String, password: String, name: String): Token {
        val response = apiService.register(
            ApiService.RegistrationRequest(login, password, name)
        )

        if (!response.isSuccessful) {
            throw ApiError("Ошибка регистрации", response.code())
        }

        return response.body() ?: throw ApiError("Пустой ответ от сервера")
    }
}

interface AuthRepository {
    suspend fun login(login: String, password: String): Token
    suspend fun register(login: String, password: String, name: String): Token
}