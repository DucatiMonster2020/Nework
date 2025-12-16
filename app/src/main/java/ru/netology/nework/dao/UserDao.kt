package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.UserEntity


@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY name ASC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM UserEntity WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(users: List<UserEntity>)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM UserEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM UserEntity")
    suspend fun clear()
}