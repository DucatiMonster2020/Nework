package ru.netology.nework.dto

import ru.netology.nework.enumeration.AttachmentType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: Instant,
    val coords: Coordinates? = null,
    val link: String? = null,
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val users: Map<Long, UserPreview> = emptyMap(),
    val ownedByMe: Boolean = false
) {
    val formattedPublished: String
        get() {
            val formatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            return formatter.format(published)
        }
    fun getLikesCount(): Int = likeOwnerIds.size
}
data class Coordinates(
    val lat: Double,
    val long: Double
)
data class Attachment(
    val url: String,
    val type: AttachmentType
)