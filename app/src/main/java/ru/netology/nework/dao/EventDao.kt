package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY datetime DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity ORDER BY datetime DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun clear()

    @Query("SELECT MAX(datetime) FROM EventEntity")
    suspend fun getMaxDatetime(): Long?
}