package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    val data: Flow<PagingData<Post>> = repository.getPagingSource()
        .cachedIn(viewModelScope)

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _edited = MutableLiveData<Post?>(null)
    val edited: LiveData<Post?> = _edited

    private val _media = MutableLiveData<MediaUpload?>(null)
    val media: LiveData<MediaUpload?> = _media

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _posts.value = repository.getAll()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
                loadPosts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun dislikeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.dislikeById(id)
                loadPosts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                loadPosts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun save() {
        _edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    _media.value?.let { mediaUpload ->
                        repository.saveWithAttachment(post, mediaUpload)
                    } ?: repository.save(post)

                    _postCreated.value = Unit
                    _edited.value = null
                    _media.value = null
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
    }

    fun edit(post: Post) {
        _edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_edited.value?.content == text) {
            return
        }
        _edited.value = _edited.value?.copy(content = text)
    }

    fun changeMedia(mediaUpload: MediaUpload) {
        _media.value = mediaUpload
    }

    fun clearMedia() {
        _media.value = null
    }
}