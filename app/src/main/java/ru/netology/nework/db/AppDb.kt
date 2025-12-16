package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.UserEntity

@Database(
    entities = [
        PostEntity::class,
        EventEntity::class,
        UserEntity::class,
        JobEntity::class,
        PostRemoteKeyEntity::class,
        EventRemoteKeyEntity::class
               ],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
}