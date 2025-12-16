package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<EventRemoteKeyEntity>)

    @Query("SELECT * FROM EventRemoteKeyEntity WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: Long): EventRemoteKeyEntity?

    @Query("SELECT * FROM EventRemoteKeyEntity ORDER BY eventId DESC LIMIT 1")
    suspend fun getLAstKey(): EventRemoteKeyEntity?

    @Query("SELECT * FROM EventRemoteKeyEntity ORDER BY eventId ASC LIMIT 1")
    suspend fun getFirstKey(): EventRemoteKeyEntity?

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM EventRemoteKeyEntity WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: Long)
}