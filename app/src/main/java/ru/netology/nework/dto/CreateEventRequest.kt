package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType

data class CreateEventRequest(
    val content: String,
    val datetime: String,
    val type: EventType = EventType.ONLINE,
    val coords: Coordinates? = null,
    val link: String? = null,
    val attachmentId: Long? = null,
    val speakerIds: List<Long>? = null
)