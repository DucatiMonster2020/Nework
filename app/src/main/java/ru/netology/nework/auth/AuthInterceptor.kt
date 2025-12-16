package ru.netology.nework.auth

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.auth.AuthState
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val appAuth: AppAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authState = appAuth.getAuthState()

        val request = if (authState is AuthState.Authorized) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer ${authState.token}")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}