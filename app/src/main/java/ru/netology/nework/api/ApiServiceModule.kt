package ru.netology.nework.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.provideAuthInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Singleton
    @Provides
    fun provideRetrofit(auth: AppAuth): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.BASE_URL}api/")
            .client(provideOkHttpClient(auth))
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
    }
    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    private fun provideOkHttpClient(auth: AppAuth): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(provideAuthInterceptor(auth))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    private fun provideGson() = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
}