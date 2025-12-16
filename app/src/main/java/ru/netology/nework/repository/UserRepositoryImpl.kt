package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.error.asAppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val jobDao: JobDao
) : UserRepository {
    override suspend fun getAll(): List<User> {
        try {
            val users = apiService.getUsers()
            userDao.insert(users.map(UserEntity::fromDto))
            return users
        } catch (e: Exception) {
            val localUsers = userDao.getAll()
            return emptyList()
        }
    }

    override suspend fun getUserById(id: Long): UserResponse {
        try {
            return apiService.getUserById(id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun getUserWall(userId: Long, offset: Int, count: Int): List<Post> {
        try {
            return apiService.getUserWall(userId,offset, count)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun getUserJobs(userId: Long): List<Job> {
        try {
            val jobs = apiService.getUserJobs(userId)
            jobDao.insert(jobs.map { JobEntity.fromDto(it, userId) })
            return jobs
        } catch (e: Exception) {
            val localJobs = jobDao.getJobsByUserIdSync(userId)
            return localJobs.map { it.toDto() }
        }
    }

    override suspend fun getMyJobs(): List<Job> {
        try {
            return apiService.getMyJobs()
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun saveJOb(job: Job): Job {
        try {
            val saved = apiService.saveJob(job)
            jobDao.insert(JobEntity.fromDto(saved, saved.id))
            return saved
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun deleteJob(id: Long) {
        try {
            apiService.deleteJob(id)
            jobDao.removeById(id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun updateAvatar(mediaUpload: MediaUpload): UserResponse {
        try {
            val mediaResponse = MediaResponse(mediaUpload.id, "")
            return apiService.updateAvatar(mediaResponse)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun getMyWall(offset: Int, count: Int): List<Post> {
        try {
            return apiService.getMyWall(offset, count)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }
    fun getUsersFlow(): Flow<List<User>> {
        return userDao.getAll().map { users ->
            users.map { it.toDto() }
        }
    }
    fun getUserJobsFlow(userId: Long): Flow<List<Job>> {
        return jobDao.getJobsByUserId(userId).map { jobs ->
            jobs.map { it.toDto() }
        }
    }
}
