package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Query("SELECT * FROM PostRemoteKeyEntity WHERE postId = :postId")
    suspend fun getByPostId(postId: Long): PostRemoteKeyEntity?

    @Query("SELECT * FROM PostRemoteKeyEntity ORDER BY postId DESC LIMIT 1")
    suspend fun getLastKey(): PostRemoteKeyEntity?

    @Query("SELECT * FROM PostRemoteKeyEntity ORDER BY postId ASC LIMIT 1")
    suspend fun getFirstKey(): PostRemoteKeyEntity?

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM PostRemoteKeyEntity WHERE postId = :postId")
    suspend fun deleteByPostId(postId: Long)
}