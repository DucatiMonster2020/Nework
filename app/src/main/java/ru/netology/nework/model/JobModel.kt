package ru.netology.nework.model

import ru.netology.nework.dto.Job
import ru.netology.nework.error.ApiError

data class JobModel(
    val job: Job? = null,
    val loading: Boolean = false,
    val error: ApiError? = null,
    val isEditing: Boolean = false
)