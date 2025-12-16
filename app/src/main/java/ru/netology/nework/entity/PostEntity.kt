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
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: Instant,
    val coords: Coordinates?,
    val link: String?,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val ownedByMe: Boolean,
    val users: Map<Long, UserPreview>
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
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment,
        ownedByMe = ownedByMe,
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
            mentionIds = dto.mentionIds,
            mentionedMe = dto.mentionedMe,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            attachment = dto.attachment,
            ownedByMe = dto.ownedByMe,
            users = dto.users
        )
    }
}
fun List<PostEntity>.toDto(): List<Post> = map { it.toDto() }
fun List<Post>.toEntity(): List<PostEntity> = map { PostEntity.fromDto(it) }
