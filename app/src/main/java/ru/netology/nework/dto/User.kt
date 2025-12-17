package ru.netology.nework.dto

data class User(
    val id: Long = 0,
    val login: String = "",
    val name: String = "",
    val avatar: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class UserPreview(
    val id: Long = 0,
    val name: String = "",
    val avatar: String? = null
)

data class Job(
    val id: Long = 0,
    val name: String = "",
    val position: String = "",
    val start: String = "",
    val finish: String? = null,
    val link: String? = null
)