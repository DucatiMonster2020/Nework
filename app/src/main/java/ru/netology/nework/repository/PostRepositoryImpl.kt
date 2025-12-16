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
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.error.asAppError
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 10
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    private val db: AppDb,
    private val postRemoteKeyDao: PostRemoteKeyDao
): PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { dao.pagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            db = db,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao
        )
    ).flow.map { pagingData ->
        pagingData.map { it.toDto() }
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getPosts(0, 100)
            dao.insert(response.map(PostEntity::fromDto))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun save(post: Post) {
        try {
            val postRequest = PostRequest(
                id = post.id,
                content = post.content,
                coords = post.coords,
                link = post.link,
                mentionIds = post.mentionIds,
                attachment = post.attachment?.let { attachment ->
                    MediaUpload(id = attachment.url)
                }
            )
            val saved = apiService.savePost(postRequest)
            dao.insert(PostEntity.fromDto(saved))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            apiService.deletePost(id)
            dao.removeById(id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val likedPost = apiService.likePost(id)
            dao.insert(PostEntity.fromDto(likedPost))
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val unlikedPost = apiService.unlikePost(id)
            dao.insert(PostEntity.fromDto(unlikedPost))
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

    override suspend fun getPostById(id: Long): Post {
        try {
            return apiService.getPostById(id)
        } catch (e: Exception) {
            throw e.asAppError()
        }
    }
}