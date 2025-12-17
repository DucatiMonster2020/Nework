package ru.netology.nework.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import ru.netology.nework.util.ConfigUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyInterceptor @Inject constructor(
    @ApplicationContext
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = ConfigUtils.getApiKey(context)
        val request = chain.request()
            .newBuilder()
            .addHeader("Api-Key", apiKey)
            .build()
        return chain.proceed(request)
    }
}