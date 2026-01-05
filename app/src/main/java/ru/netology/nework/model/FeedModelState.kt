package ru.netology.nework.model

sealed interface FeedModelState {
    object Idle : FeedModelState
    object Loading : FeedModelState
    object Refreshing : FeedModelState
    data class Error(val message: String) : FeedModelState
    object Success : FeedModelState
}

fun FeedModelState.isLoading() = this is FeedModelState.Loading
fun FeedModelState.isError() = this is FeedModelState.Error