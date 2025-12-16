package ru.netology.nework.dto

data class CreatePostRequest(
    val content: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val attachmentId: Long? = null,
    val mentionIds: List<Long>? = null
)