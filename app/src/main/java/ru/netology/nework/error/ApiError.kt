package ru.netology.nework.error

import java.io.IOException

class ApiError(
    message: String,
    val status: Int = 0
) : IOException(message)