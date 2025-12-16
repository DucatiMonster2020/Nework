package ru.netology.nework.repository

import ru.netology.nework.dto.Job
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse

interface UserRepository {
    suspend fun getAll(): List<User>
    suspend fun getUserById(id: Long): UserResponse
    suspend fun getUserWall(userId: Long, offset: Int, count: Int): List<Post>
    suspend fun getUserJobs(userId: Long): List<Job>
    suspend fun getMyJobs(): List<Job>
    suspend fun saveJOb(job: Job): Job
    suspend fun deleteJob(id: Long)
    suspend fun updateAvatar(mediaUpload: MediaUpload): UserResponse
    suspend fun getMyWall(offset: Int, count: Int): List<Post>
}