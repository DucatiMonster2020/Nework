package ru.netology.nework.util

import ru.netology.nework.BuildConfig

object ConfigUtils {

    fun getApiKey(): String {
        return try {
            BuildConfig.API_KEY.ifBlank { "c1378193-bc0e-42c8-a502-b8d66di" }
        } catch (e: Exception) {
            "c1378193-bc0e-42c8-a502-b8d66di"
        }
    }

    fun getBaseUrl(): String {
        return try {
            BuildConfig.BASE_URL.ifBlank { "http://94.228.125.136:8080/" }
        } catch (e: Exception) {
            "http://94.228.125.136:8080/"
        }
    }
}