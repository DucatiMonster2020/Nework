package ru.netology.nework.model

import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.User
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.error.AppError
import java.time.Instant

data class EventModel(
    val events: List<Event> = emptyList(),
    val loading: Boolean = false,
    val error: AppError? = null,
    val refreshing: Boolean = false,
    val empty: Boolean = false
) {
    val isLoading: Boolean get() = loading
    val isRefreshing: Boolean get() = refreshing
}
data class EventDetailModel(
    val event: Event? = null,
    val loading: Boolean = false,
    val error: AppError? = null,
    val participants: List<User> = emptyList(),
    val speakers: List<User> = emptyList(),
    val isLiked: Boolean = false,
    val isParticipating: Boolean = false
)
data class EventContentModel(
    val content: String = "",
    val datetime: Instant? = null,
    val type: EventType = EventType.ONLINE,
    val coordinates: Coordinates? = null,
    val attachment: Attachment? = null,
    val link: String? = null,
    val speakerIds: List<Long> = emptyList(),
    val selectedSpeakers: List<User> = emptyList()
) {
    fun isEmpty(): Boolean = content.isBlank() &&
            datetime == null &&
            coordinates == null &&
            attachment == null &&
            link.isNullOrBlank() &&
            speakerIds.isEmpty()
}