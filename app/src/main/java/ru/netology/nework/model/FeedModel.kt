package ru.netology.nework.model

import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.error.AppError

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: AppError? = null,
    val refreshing: Boolean = false,
    val empty: Boolean = false
) {
    val isLoading: Boolean get() = loading
    val isRefreshing: Boolean get() = refreshing
}
data class PostModel(
    val post: Post? = null,
    val loading: Boolean = false,
    val error: AppError? = null,
    val users: Map<Long, User> = emptyMap(),
    val isLiked: Boolean = false
)
data class PostContentModel(
    val content: String = "",
    val link: String? = null,
    val coordinates: Coordinates? = null,
    val attachment: Attachment? = null,
    val mentionIds: List<Long> = emptyList(),
    val selectedUsers: List<User> = emptyList()
) {
    fun isEmpty(): Boolean = content.isBlank() &&
            link.isNullOrBlank() &&
            coordinates == null &&
            attachment == null &&
            mentionIds.isEmpty()
}