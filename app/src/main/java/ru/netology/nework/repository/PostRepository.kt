package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun uploadMedia(file: File): MediaUpload
    suspend fun getPostById(id: Long): Post
}
