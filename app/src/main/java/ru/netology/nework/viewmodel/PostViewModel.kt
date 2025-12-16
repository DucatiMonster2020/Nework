package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.error.AppError
import ru.netology.nework.error.asAppError
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.PostContentModel
import ru.netology.nework.repository.*
import ru.netology.nework.util.SingleLiveEvent
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth
) : ViewModel() {
    private val _feedState = MutableLiveData(FeedModel())
    val feedState: LiveData<FeedModel> = _feedState
    private val _postContentState = MutableLiveData(PostContentModel())
    val postContentState: LiveData<PostContentModel> = _postContentState
    private val _errorState = SingleLiveEvent<AppError>()
    val errorState: LiveData<AppError> = _errorState

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = auth
        .authStateFlow
        .flatMapLatest { (token, _) ->
            repository.data.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == auth.authStateFlow.value.userId)
                }
            }
        }
    fun loadPosts() {
        viewModelScope.launch {
            try {
                _feedState.value = FeedModel(loading = true)
                repository.getAll()
                _feedState.value = FeedModel(loading = false)
            } catch (e: Exception) {
                _feedState.value = FeedModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun refresh() {
        viewModelScope.launch {
            try {
                _feedState.value = FeedModel(refreshing = true)
                repository.getAll()
                _feedState.value = FeedModel(refreshing = false)
            } catch (e: Exception) {
                _feedState.value = FeedModel(refreshing = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun likeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.likeById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun unlikeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.unlikeById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun removeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun save(post: PostRequest) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val postToSave = Post(
                    id = post.id,
                    authorId = auth.authStateFlow.value.userId,
                    author = "",
                    content = post.content,
                    published = Instant.now(),
                    coords = post.coords,
                    link = post.link,
                    mentionIds = post.mentionIds
                )
                repository.save(postToSave)
                clearContent()
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun updateContent(content: String) {
        _postContentState.value = _postContentState.value?.copy(content = content)
    }
    fun updateLink(link: String?) {
        _postContentState.value = _postContentState.value?.copy(link = link)
    }
    fun updateCoordinates(coords: Coordinates?) {
        _postContentState.value = _postContentState.value?.copy(coordinates = coords)
    }
    fun updateMentionIds(mentionIds: List<Long>) {
        _postContentState.value = _postContentState.value?.copy(mentionIds = mentionIds)
    }
    fun clearContent() {
        _postContentState.value = PostContentModel()
    }
    fun loadPostById(id: Long) {
        viewModelScope.launch {
            try {
                val post = repository.getPostById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
}