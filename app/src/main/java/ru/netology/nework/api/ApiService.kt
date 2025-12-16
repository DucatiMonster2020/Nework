package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.AuthenticationResponse
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.LoginRequest
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.dto.RegisterRequest
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse

interface ApiService {
    @POST("api/users/authentication")
    suspend fun login(@Body loginRequest: LoginRequest): AuthenticationResponse
    @POST("api/users/registration")
    suspend fun register(@Body registerRequest: RegisterRequest): AuthenticationResponse

    @GET("api/posts")
    suspend fun getPosts(@Query("offset") offset: Int, @Query("count") count: Int): List<Post>
    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Post
    @GET("api/posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Long): List<Post>
    @POST("api/posts")
    suspend fun savePost(@Body post: PostRequest): Post
    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)
    @POST("api/posts/{id}/likes")
    suspend fun likePost(@Path("id") id: Long): Post
    @DELETE("api/posts/{id}/likes")
    suspend fun unlikePost(@Path("id") id: Long): Post

    @GET("api/events")
    suspend fun getEvents(@Query("offset") offset: Int, @Query("count") count: Int): List<Event>
    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Event
    @POST("api/events")
    suspend fun saveEvent(@Body event: EventRequest): Event
    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long)
    @POST("api/events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Long): Event
    @DELETE("api/events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") id: Long): Event
    @POST("api/events/{id}/participants")
    suspend fun participateInEvent(@Path("id") id: Long): Event
    @DELETE("api/events/{id}/participants")
    suspend fun unparticipateInEvent(@Path("id") id: Long): Event

    @GET("api/users")
    suspend fun getUsers(): List<User>
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserResponse
    @GET("api/users/{id}/wall")
    suspend fun getUserWall(@Path("id") id: Long, @Query("offset") offset: Int, @Query("count") count: Int): List<Post>
    @GET("api/users/{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: Long): List<Job>

    @Multipart
    @POST("api/media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): MediaResponse
    @GET("api/my/jobs")
    suspend fun getMyJobs(): List<Job>
    @POST("api/my/jobs")
    suspend fun saveJob(@Body job: Job): Job
    @DELETE("api/my/jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Long)
    @PUT("api/users/avatar")
    suspend fun updateAvatar(@Body media: MediaResponse): UserResponse
    @GET("api/my/wall")
    suspend fun getMyWall(@Query("offset") offset: Int, @Query("count") count: Int): List<Post>
}