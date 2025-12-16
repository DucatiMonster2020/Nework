package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.*
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState> = _dataState

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _postCreated = MutableLiveData(false)
    val postCreated: LiveData<Boolean> = _postCreated

    private val _currentPost = MutableLiveData<Post?>()
    val currentPost: LiveData<Post?> = _currentPost

    private val _coordinates = MutableLiveData<Coords?>(null)
    val coordinates: LiveData<Coords?> = _coordinates

    val data: Flow<PagingData<Post>> = repository.data
        .cachedIn(viewModelScope)

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // Paging 3 автоматически загружает данные
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки постов"
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при лайке"
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.unlikeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при удалении лайка"
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при удалении поста"
        }
    }

    fun createPost(
        content: String,
        mediaUpload: MediaUpload? = null,
        link: String? = null,
        mentionIds: List<Long> = emptyList()
    ) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)

            val coords = _coordinates.value

            repository.save(
                content = content,
                mediaUpload = mediaUpload,
                link = link,
                coords = coords,
                mentionIds = mentionIds
            )

            _postCreated.value = true
            _coordinates.value = null
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка при создании поста"
            _postCreated.value = false
        }
    }

    fun updatePost(
        id: Long,
        content: String,
        mediaUpload: MediaUpload? = null,
        link: String? = null,
        mentionIds: List<Long> = emptyList()
    ) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)

            val coords = _coordinates.value
            repository.update(
                id = id,
                content = content,
                mediaUpload = mediaUpload,
                link = link,
                coords = coords,
                mentionIds = mentionIds
            )

            _postCreated.value = true
            _coordinates.value = null
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка при обновлении поста"
            _postCreated.value = false
        }
    }

    fun loadPost(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            val post = repository.getById(id)
            _currentPost.value = post
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки поста"
        }
    }

    fun setCoordinates(lat: Double, long: Double) {
        _coordinates.value = Coords(lat.toString(), long.toString())
    }

    fun clearCoordinates() {
        _coordinates.value = null
    }

    fun uploadMedia(uri: Uri): Flow<String> {
        return repository.uploadMedia(uri).map { media ->
            media.url
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            repository.refresh()
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка обновления"
        }
    }
}