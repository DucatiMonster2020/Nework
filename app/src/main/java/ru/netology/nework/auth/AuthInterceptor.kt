package ru.netology.nework.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val auth: AppAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = auth.authStateFlow.value.token

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

fun provideAuthInterceptor(auth: AppAuth): Interceptor = AuthInterceptor(auth)