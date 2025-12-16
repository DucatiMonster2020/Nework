package ru.netology.nework.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.BuildConfig
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.db.AppDb
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val API_KEY = BuildConfig.API_KEY
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
    }
    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
    @Singleton
    @Provides
    fun provideOkHttpClient(auth: AppAuth): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Api-Key", API_KEY)
                auth.authStateFlow.value.token?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    @Singleton
    @Provides
    fun provideAppDb(@ApplicationContext context: Context): AppDb {
        return databaseBuilder(context, AppDb::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides
    @Singleton
    fun provideAppAuth(@ApplicationContext context: Context): AppAuth {
        return AppAuth(context)
    }
}