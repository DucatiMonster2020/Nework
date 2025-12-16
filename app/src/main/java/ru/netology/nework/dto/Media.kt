package ru.netology.nework.dto

import java.io.File

data class MediaUpload(
    val file: File
)
data class MediaResponse(
    val id: String,
    val url: String
)