package ru.netology.nework.dto

data class MediaUpload(
    val id: String
)
data class MediaResponse(
    val id: String,
    val url: String
)
data class PhotoModel(
    val uri: String? = null
)