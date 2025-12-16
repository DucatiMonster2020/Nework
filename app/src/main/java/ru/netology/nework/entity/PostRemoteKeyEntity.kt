package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity (
    @PrimaryKey(autoGenerate = false)
    val postId: Long,
    val nextKey: Int?
)