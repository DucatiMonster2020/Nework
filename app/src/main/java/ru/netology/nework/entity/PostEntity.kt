package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.db.Converters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview
import java.time.Instant

@Entity
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: Instant,
    val coords: Coordinates? = null,
    val link: String? = null,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val users: Map<Long, UserPreview> = emptyMap()
    ) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        published = published,
        coords = coords,
        link = link,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        users = users
    )
    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorAvatar = dto.authorAvatar,
            authorJob = dto.authorJob,
            content = dto.content,
            published = dto.published,
            coords = dto.coords,
            link = dto.link,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            attachment = dto.attachment,
            mentionIds = dto.mentionIds,
            mentionedMe = dto.mentionedMe,
            users = dto.users
        )
    }
}
