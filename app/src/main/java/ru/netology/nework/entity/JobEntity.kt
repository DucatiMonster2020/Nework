package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.db.Converters
import ru.netology.nework.dto.Job
import java.time.Instant

@Entity
@TypeConverters(Converters::class)
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val position: String,
    val start: Instant,
    val finish: Instant? = null,
    val link: String? = null
) {
    fun toDto() = Job(
        id = id,
        userId = userId,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )
    companion object {
        fun fromDto(dto: Job, userId: Long) = JobEntity(
            id = dto.id,
            userId = dto.userId,
            name = dto.name,
            position = dto.position,
            start = dto.start,
            finish = dto.finish,
            link = dto.link
        )
    }
}