package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE userId = :userId ORDER BY start DESC")
    fun getJobsByUserId(userId: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity WHERE userId = :userId AND userId = :userId")
    suspend fun getJobsByUserIdSync(userId: Long): List<JobEntity>

    @Query("SELECT * FROM JobEntity WHERE id = :id")
    suspend fun getById(id: Long): JobEntity?

    @Query("SELECT * FROM JobEntity WHERE userId = :userId AND finish IS NULL LIMIT 1")
    suspend fun getCurrentJob(userId: Long): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Update
    suspend fun update(job: JobEntity)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM JobEntity WHERE userId = :userId")
    suspend fun removeByUserId(userId: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun clear()
}