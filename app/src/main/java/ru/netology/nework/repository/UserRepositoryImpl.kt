package ru.netology.nework.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.error.asAppError
import ru.netology.nework.util.AndroidUtils
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: UserDao,
    appDb: AppDb,
) : UserRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = dao::pagingSource
    ).flow
        .map { pagingData ->
            pagingData.map { it.toDto() }
        }
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long): User {
        try {
            // Сначала проверяем кэш
            val cachedUser = dao.getById(id)?.toDto()
            if (cachedUser != null) {
                return cachedUser
            }

            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(UserEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getMyProfile(): User {
        try {
            val response = apiService.getMyProfile()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(UserEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJobs(userId: Long): List<Job> {
        try {
            val response = apiService.getJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: emptyList()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getMyJobs(): List<Job> {
        try {
            val response = apiService.getMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: emptyList()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(
        company: String,
        position: String,
        startDate: String,
        endDate: String?
    ) {
        try {
            val response = apiService.saveJob(
                CreateJobRequest(
                    name = company,
                    position = position,
                    start = startDate,
                    finish = endDate
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun updateJob(
        id: Long,
        company: String,
        position: String,
        startDate: String,
        endDate: String?
    ) {
        try {
            val response = apiService.updateJob(
                id,
                CreateJobRequest(
                    name = company,
                    position = position,
                    start = startDate,
                    finish = endDate
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeJob(id: Long) {
        try {
            val response = apiService.removeJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun updateAvatar(uri: Uri) {
        try {
            val file = File(AndroidUtils.getFilePathFromUri(uri) ?: return)
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )

            val response = apiService.updateAvatar(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(UserEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun follow(id: Long) {
        try {
            val response = apiService.followUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unfollow(id: Long) {
        try {
            val response = apiService.unfollowUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun refresh() {
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}