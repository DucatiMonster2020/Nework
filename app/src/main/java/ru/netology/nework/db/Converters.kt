package ru.netology.nework.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.UserPreview

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCoords(coords: Coords?): String? =
        coords?.let { gson.toJson(it) }

    @TypeConverter
    fun toCoords(json: String?): Coords? =
        json?.let { gson.fromJson(it, Coords::class.java) }

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? =
        attachment?.let { gson.toJson(it) }

    @TypeConverter
    fun toAttachment(json: String?): Attachment? =
        json?.let { gson.fromJson(it, Attachment::class.java) }

    @TypeConverter
    fun fromUserPreviewMap(map: Map<Long, UserPreview>): String =
        gson.toJson(map)

    @TypeConverter
    fun toUserPreviewMap(json: String): Map<Long, UserPreview> =
        gson.fromJson(json, object : TypeToken<Map<Long, UserPreview>>() {}.type)

    @TypeConverter
    fun fromLongList(list: List<Long>): String =
        gson.toJson(list)

    @TypeConverter
    fun toLongList(json: String): List<Long> =
        gson.fromJson(json, object : TypeToken<List<Long>>() {}.type)
}
