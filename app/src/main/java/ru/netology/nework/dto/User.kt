package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class User(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
    val following: Boolean = false,
    val followers: Int = 0,
    val followingCount: Int = 0
)
data class UserResponse(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
    val lastJob: Job? = null
)
data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: Instant,
    val finish: Instant? = null,
    val link: String? = null
) {
    val formattedPeriod: String
        get() {
            val startFormatter = DateTimeFormatter
                .ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault())
            val finisheFormatter = DateTimeFormatter
                .ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault())
            return if (finish != null) {
                "${startFormatter.format(start)} - ${finisheFormatter.format(finish)}"
            } else {
                "${startFormatter.format(start)} - настоящее время"
            }
        }
}
data class RegisterRequest(
    val login: String,
    val pass: String,
    val name: String? = null,
    val file: MediaUpload? = null
)
data class LoginRequest(
    val login: String,
    val pass: String
)
data class AuthenticationResponse(
    val id: Long,
    val token: String
)
data class PostRequest(
    val id: Long = 0,
    val content: String,
    val coords: Coords? = null,
    val link: String? = null,
    val attachment: MediaUpload? = null,
    val mentionIds: List<Long> = emptyList()
)
data class EventRequest(
    val id: Long = 0,
    val content: String,
    val datetime: Instant? = null,
    val coords: Coords? = null,
    val type: EventType? = null,
    val attachment: MediaUpload? = null,
    val link: String? = null,
    val speakerIds: List<Long> = emptyList()
)
