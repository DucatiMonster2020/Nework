package ru.netology.nework.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import java.time.Instant
import java.time.LocalDateTime

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it)}

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromCoordinates(value: Coordinates?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toCoordinates(value: String?): Coordinates? = value?.let { gson.fromJson(it, Coordinates::class.java) }

    @TypeConverter
    fun fromAttachment(value: Attachment?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toAttachment(value: String?): Attachment? = value?.let { gson.fromJson(it, Attachment::class.java) }

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType?): String? = value?.name

    @TypeConverter
    fun toAttachmentType(value: String?): AttachmentType? = value?.let { AttachmentType.valueOf(it) }

    @TypeConverter
    fun fromEventType(value: EventType?): String? = value?.name

    @TypeConverter
    fun toEventType(value: String?): EventType? = value?.let { EventType.valueOf(it) }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLongList(value: String?): List<Long> = value?.let { gson.fromJson(it, object : TypeToken<List<Long>>() {}.type) } ?: emptyList()

    @TypeConverter
    fun fromUserMap(value: Map<Long, UserPreview>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toUserMap(value: String?): Map<Long, UserPreview> = value?.let { gson.fromJson(it, object : TypeToken<Map<Long, UserPreview>>() {}.type)} ?: emptyMap()
}
