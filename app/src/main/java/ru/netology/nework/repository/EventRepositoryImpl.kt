package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 10
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao,
    private val apiService: ApiService,
    private val db: AppDb,
    private val eventRemoteKeyDao: EventRemoteKeyDao
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { dao.pagingSource() },
        remoteMediator = EventRemoteMediator(apiService = apiService, db = db, eventDao = dao, eventRemoteKeyDao = eventRemoteKeyDao)
    ).flow.map { pagingData ->
        pagingData.map { it.toDto() }
    }
    override suspend fun getAll() {
        try {
            val response = apiService.getEvents(0, 100)
            dao.insert(response.map(EventEntity::fromDto))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun save(event: EventRequest) {
        try {
            val saved = apiService.saveEvent(event)
            dao.insert(EventEntity.fromDto(saved))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun removeById(id: Long) {
            try {
                apiService.deleteEvent(id)
                dao.removeById(id)
            } catch (e: Exception) {
                throw e.asAppError()
            }
    }

    override suspend fun likeById(id: Long) {
            try {
                val likedEvent = apiService.likeEvent(id)
                dao.insert(EventEntity.fromDto(likedEvent))
            } catch (e: Exception) {
                throw e.asAppError()
            }
    }

    override suspend fun unlikeById(id: Long) {
            try {
                val unlikedEvent = apiService.unlikeEvent(id)
                dao.insert(EventEntity.fromDto(unlikedEvent))
            } catch (e: Exception) {
                throw e.asAppError()
            }
    }

    override suspend fun participateById(id: Long) {
        try {
            val event = apiService.participateInEvent(id)
            dao.insert(EventEntity.fromDto(event))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun unparticipateById(id: Long) {
        try {
            val event = apiService.unparticipateInEvent(id)
            dao.insert(EventEntity.fromDto(event))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun getEventById(id: Long): Event {
        try {
            return apiService.getEventById(id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun uploadMedia(file: File): MediaUpload {
        try {
            val part = MultipartBody.Part.createFormData("file", file.name)
            val mediaResponse = apiService.uploadMedia(part)
            return MediaUpload(mediaResponse.id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }
}
