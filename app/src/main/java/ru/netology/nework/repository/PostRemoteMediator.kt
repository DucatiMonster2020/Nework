package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getPosts(0,state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        apiService.getPosts(0, state.config.pageSize)
                    } else {
                        apiService.getPosts(lastItem.id.toInt(), state.config.pageSize)
                    }
                }
            }
            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.deleteAll()
                        postDao.clear()
                    }
                    else -> Unit
                }
                val posts = response.map { PostEntity.fromDto(it) }
                postDao.insert(posts)
                val remoteKeys = posts.map { post ->
                    PostRemoteKeyEntity(
                        postId = post.id,
                        nextKey = posts.lastOrNull()?.id?.plus(1)?.toInt()
                    )
                }
                postRemoteKeyDao.insert(remoteKeys)
            }
            return MediatorResult.Success(
                endOfPaginationReached = response.isEmpty()
            )
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}
