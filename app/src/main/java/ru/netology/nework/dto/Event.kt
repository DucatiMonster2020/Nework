package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType
import java.time.Instant

data class Event(
    val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String = "",
    val datetime: Instant = Instant.now(),
    val published: Instant = Instant.now(),
    val coords: Coordinates? = null,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Long> = emptySet(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val users: Map<Long, UserPreview> = emptyMap()
)