package ru.netology.nework.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override fun getPagingSource(): PagingSource<Int, Post> = PostPagingSource(apiService)

    override suspend fun getAll() = apiService.getAllPosts()

    override suspend fun getById(id: Long) = apiService.getPostById(id)

    override suspend fun likeById(id: Long) = apiService.likeById(id)

    override suspend fun dislikeById(id: Long) = apiService.dislikeById(id)

    override suspend fun removeById(id: Long) = apiService.removeById(id)

    override suspend fun save(post: Post) = apiService.save(post)

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) = withContext(Dispatchers.IO) {
        try {
            val media = upload(upload)
            apiService.save(post.copy(attachment = media))
        } catch (e: IOException) {
            throw ApiError(e.message ?: "Network error")
        } catch (e: Exception) {
            throw ApiError(e.message ?: "Unknown error")
        }
    }

    override suspend fun upload(upload: MediaUpload): Media = withContext(Dispatchers.IO) {
        try {
            apiService.uploadMedia(upload)
        } catch (e: IOException) {
            throw ApiError(e.message ?: "Network error")
        }
    }
}

private class PostPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Post>() {

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val page = params.key ?: 0
            val size = params.loadSize

            val response = apiService.getAllPosts(page, size)
            if (!response.isSuccessful) {
                return LoadResult.Error(ApiError("HTTP ${response.code()}"))
            }

            val posts = response.body() ?: emptyList()
            val nextKey = if (posts.size < size) null else page + 1
            val prevKey = if (page > 0) page - 1 else null

            LoadResult.Page(
                data = posts,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}