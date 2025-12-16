package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: Instant,
    val published: Instant,
    val coords: Coordinates? = null,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participants: Map<Long, UserPreview> = emptyMap(),
    val speakers: Map<Long, UserPreview> = emptyMap(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false
) {
    val formattedPublished: String
        get() {
            val formatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            return formatter.format(published)
        }
    val formattedDatetime: String
        get() {
            val formatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            return formatter.format(datetime)
        }
    fun getLikesCount(): Int = likeOwnerIds.size
    fun getParticipantsCount(): Int = participantsIds.size
    fun getSpeakersCount(): Int = speakerIds.size
}