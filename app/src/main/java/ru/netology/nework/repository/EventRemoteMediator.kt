package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND ->
                    return MediatorResult.Success(true)

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        null
                    } else {
                        eventRemoteKeyDao.getByEventId(lastItem.id)?.nextKey
                    }
                }
            }
            val response =
                apiService.getEvents(offset = loadKey ?: 0, count = state.config.pageSize)
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    eventRemoteKeyDao.deleteAll()
                    eventDao.clear()
                }
                val nextKey = if (response.isEmpty()) {
                    null
                } else {
                    (loadKey ?: 0) + response.size
                }
                eventRemoteKeyDao.insert(response.map { event ->
                    EventRemoteKeyEntity(
                        eventId = event.id,
                        nextKey = nextKey
                    )
                }
                )
                eventDao.insert(response.map { EventEntity.fromDto(it) })
            }
            MediatorResult.Success(response.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
        override suspend fun initialize(): InitializeAction {
            return InitializeAction.LAUNCH_INITIAL_REFRESH
        }
}