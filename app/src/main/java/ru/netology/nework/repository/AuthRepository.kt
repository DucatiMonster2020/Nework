package ru.netology.nework.repository

import ru.netology.nework.dto.AuthenticationResponse
import ru.netology.nework.dto.MediaResponse
import java.io.File

interface AuthRepository {
    suspend fun login(login:String, password: String): AuthenticationResponse
    suspend fun register(login: String, password: String, name: String, avatar: File? = null): AuthenticationResponse
    suspend fun uploadAvatar(file: File): MediaResponse
}