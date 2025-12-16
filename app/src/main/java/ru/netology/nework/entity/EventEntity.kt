package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.db.Converters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.EventType
import java.time.Instant

@Entity
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: Instant,
    val published: Instant,
    val coords: Coords? = null,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val participants: Map<Long, UserPreview> = emptyMap(),
    val speakers: Map<Long, UserPreview> = emptyMap(),
    val ownedByMe: Boolean = false
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        datetime = datetime,
        published = published,
        coords = coords,
        type = type,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment,
        link = link,
        participants = participants,
        speakers = speakers,
        ownedByMe = ownedByMe
    )
    companion object {
        fun fromDto(dto: Event) = EventEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorAvatar = dto.authorAvatar,
            authorJob = dto.authorJob,
            content = dto.content,
            datetime = dto.datetime,
            published = dto.published,
            coords = dto.coords,
            type = dto.type,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            speakerIds = dto.speakerIds,
            participantsIds = dto.participantsIds,
            participatedByMe = dto.participatedByMe,
            attachment = dto.attachment,
            link = dto.link,
            participants = dto.participants,
            speakers = dto.speakers,
            ownedByMe = dto.ownedByMe
        )
    }
}