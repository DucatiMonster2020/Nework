package ru.netology.nework.dto

import ru.netology.nework.enumeration.AttachmentType
import java.time.Instant

data class Post(
    val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String = "",
    val published: Instant = Instant.now(),
    val coords: Coordinates? = null,
    val link: String? = null,
    val likeOwnerIds: Set<Long> = emptySet(),
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<Long, UserPreview> = emptyMap()
)

data class Coordinates(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class Attachment(
    val url: String,
    val type: AttachmentType
)

data class MediaUpload(
    val uri: String
)