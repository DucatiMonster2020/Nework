package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey(autoGenerate = false)
    val eventId: Long,
    val nextKey: Int?
)