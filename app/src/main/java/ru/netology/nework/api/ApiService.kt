package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.CreateEventRequest
import ru.netology.nework.dto.CreateJobRequest
import ru.netology.nework.dto.CreatePostRequest
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.User

interface ApiService {

    // ========== АУТЕНТИФИКАЦИЯ ==========
    @FormUrlEncoded
    @POST("api/users/authentication")
    suspend fun loginUser(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<Token>

    @Multipart
    @POST("api/users/registration")
    suspend fun registerUser(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Response<Token>

    @POST("api/users/logout")
    suspend fun logoutUser(): Response<Unit>

    // ========== ПОЛЬЗОВАТЕЛИ ==========
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @GET("api/users/my")
    suspend fun getMyProfile(): Response<User>

    @GET("api/{user_id}/wall")
    suspend fun getUserWall(@Path("user_id") userId: Long): Response<List<Post>>

    @GET("api/{user_id}/jobs")
    suspend fun getJobs(@Path("user_id") userId: Long): Response<List<Job>>

    @GET("api/my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("api/my/jobs")
    suspend fun saveJob(@Body job: CreateJobRequest): Response<Job>

    @POST("api/my/jobs/{id}")
    suspend fun updateJob(
        @Path("id") id: Long,
        @Body job: CreateJobRequest
    ): Response<Job>

    @DELETE("api/my/jobs/{id}")
    suspend fun removeJob(@Path("id") id: Long): Response<Unit>

    @Multipart
    @POST("api/users/avatar")
    suspend fun updateAvatar(@Part file: MultipartBody.Part): Response<User>

    @POST("api/users/{id}/followers")
    suspend fun followUser(@Path("id") id: Long): Response<Unit>

    @DELETE("api/users/{id}/followers")
    suspend fun unfollowUser(@Path("id") id: Long): Response<Unit>

    // ========== ПОСТЫ ==========
    @GET("api/posts")
    suspend fun getAllPosts(
        @Query("count") count: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<List<Post>>

    @GET("api/posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<Post>>

    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("api/posts")
    suspend fun savePost(@Body post: CreatePostRequest): Response<Post>

    @POST("api/posts/{id}/likes")
    suspend fun likeByIdPost(@Path("id") id: Long): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikeByIdPost(@Path("id") id: Long): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun removeByIdPost(@Path("id") id: Long): Response<Unit>

    @POST("api/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Long,
        @Body post: CreatePostRequest
    ): Response<Post>

    // ========== СОБЫТИЯ ==========
    @GET("api/events")
    suspend fun getAllEvents(
        @Query("count") count: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<List<Event>>

    @GET("api/events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("api/events")
    suspend fun saveEvent(@Body event: CreateEventRequest): Response<Event>

    @POST("api/events/{id}/likes")
    suspend fun likeByIdEvent(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun unlikeByIdEvent(@Path("id") id: Long): Response<Event>

    @POST("api/events/{id}/participants")
    suspend fun participate(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/participants")
    suspend fun unparticipate(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun removeByIdEvent(@Path("id") id: Long): Response<Unit>

    @POST("api/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Long,
        @Body event: CreateEventRequest
    ): Response<Event>

    // ========== МЕДИА ==========
    @Multipart
    @POST("api/media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<MediaStore.Video.Media>
}