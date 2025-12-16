package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.dto.MediaUpload
import java.io.File

interface EventRepository {
    val data: Flow<PagingData<Event>>
    suspend fun getAll()
    suspend fun save(event: EventRequest)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun participateById(id: Long)
    suspend fun unparticipateById(id: Long)
    suspend fun getEventById(id: Long): Event
    suspend fun uploadMedia(file: File): MediaUpload
}