package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.User

interface ApiService {
    // ========== АУТЕНТИФИКАЦИЯ ==========
    @POST("api/users/authentication")
    suspend fun login(@Body auth: AuthRequest): Response<Token>

    @POST("api/users/registration")
    suspend fun register(@Body registration: RegistrationRequest): Response<Token>

    // ========== ПОСТЫ ==========
    @GET("api/posts")
    suspend fun getAllPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Post>>

    @GET("api/posts/latest/{count}")
    suspend fun getLatestPosts(@Path("count") count: Int): Response<List<Post>>

    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("api/posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun removePostById(@Path("id") id: Long): Response<Unit>

    @POST("api/posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") id: Long): Response<Post>

    // ========== СОБЫТИЯ ==========
    @GET("api/events")
    suspend fun getAllEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Event>>

    @GET("api/events/latest/{count}")
    suspend fun getLatestEvents(@Path("count") count: Int): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("api/events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("api/events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<Event>

    @POST("api/events/{id}/participants")
    suspend fun participateInEvent(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/participants")
    suspend fun cancelParticipation(@Path("id") id: Long): Response<Event>

    // ========== ПОЛЬЗОВАТЕЛИ ==========
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @GET("api/users/{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: Long): Response<List<Job>>

    @GET("api/users/{id}/wall")
    suspend fun getUserWall(
        @Path("id") id: Long, @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Post>>

    @GET("api/users/{id}/events")
    suspend fun getUserEvents(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Event>>

    // ========== МОИ ДАННЫЕ ==========
    @GET("api/my/wall")
    suspend fun getMyWall(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Post>>

    @GET("api/my/events")
    suspend fun getMyEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Event>>

    @GET("api/my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("api/my/jobs")
    suspend fun saveMyJob(@Body job: Job): Response<Job>

    @PUT("api/my/jobs/{id}")
    suspend fun updateMyJob(
        @Path("id") id: Long,
        @Body job: Job
    ): Response<Job>

    @DELETE("api/my/jobs/{id}")
    suspend fun removeMyJob(@Path("id") id: Long): Response<Unit>

    // ========== МЕДИА ==========
    @Multipart
    @POST("api/media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<Media>

    // ========== DTO для запросов ==========
    data class AuthRequest(
        val login: String,
        val password: String
    )

    data class RegistrationRequest(
        val login: String,
        val password: String,
        val name: String
    )
}