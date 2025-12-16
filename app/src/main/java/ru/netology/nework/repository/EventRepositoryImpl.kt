package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.error.asAppError
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: EventDao,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = EventRemoteMediator(
            apiService = apiService,
            eventDao = dao,
            eventRemoteKeyDao = eventRemoteKeyDao,
            appDb = appDb,
        ),
        pagingSourceFactory = dao::pagingSource
    ).flow
        .map { pagingData ->
            pagingData.map { it.toDto() }
        }
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getAllEvents()
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

    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeByIdEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val response = apiService.unlikeByIdEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun participate(id: Long) {
        try {
            val response = apiService.participate(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unparticipate(id: Long) {
        try {
            val response = apiService.unparticipate(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeByIdEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(
        content: String,
        datetime: String,
        type: EventType,
        mediaUpload: MediaUpload?,
        link: String?,
        coords: Coordinates?,
        speakerIds: List<Long>
    ) {
        try {
            val media = mediaUpload?.let { upload ->
                upload(MediaUpload(upload.file))
            }

            val response = apiService.saveEvent(
                CreateEventRequest(
                    content = content,
                    datetime = datetime,
                    type = type,
                    coords = coords,
                    link = link,
                    attachmentId = media?.id,
                    speakerIds = if (speakerIds.isEmpty()) null else speakerIds
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun update(
        id: Long,
        content: String,
        datetime: String,
        type: EventType,
        mediaUpload: MediaUpload?,
        link: String?,
        coords: Coordinates?,
        speakerIds: List<Long>
    ) {
        try {
            val media = mediaUpload?.let { upload ->
                upload(MediaUpload(upload.file))
            }

            val response = apiService.updateEvent(
                id,
                CreateEventRequest(
                    content = content,
                    datetime = datetime,
                    type = type,
                    coords = coords,
                    link = link,
                    attachmentId = media?.id,
                    speakerIds = if (speakerIds.isEmpty()) null else speakerIds
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long): Event {
        try {
            val response = apiService.getEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun uploadMedia(upload: MediaUpload): Media {
        try {
            val part = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )

            val response = apiService.uploadMedia(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun refresh() {
        try {
            val response = apiService.getLatestEvents(10)
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

    private suspend fun upload(mediaUpload: MediaUpload): Media {
        return uploadMedia(mediaUpload)
    }
}
