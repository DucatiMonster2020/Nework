package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState> = _dataState

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs

    private val _lastJob = MutableLiveData<Job?>()
    val lastJob: LiveData<Job?> = _lastJob

    val users: Flow<PagingData<User>> = userRepository.data
        .cachedIn(viewModelScope)

    val wallPosts: Flow<PagingData<Post>> = postRepository.wallData
        .cachedIn(viewModelScope)

    fun loadUsers() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // Paging 3 автоматически загружает данные
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки пользователей"
        }
    }

    fun loadUser(id: Long) = viewModelScope.launch {
        try {
            _loading.value = true
            val user = userRepository.getById(id)
            _user.value = user
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка загрузки пользователя"
        }
    }

    fun loadUserWall(userId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // Paging 3 автоматически загружает данные
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки стены"
        }
    }

    fun loadUserJobs(userId: Long) = viewModelScope.launch {
        try {
            _loading.value = true
            val jobsList = userRepository.getJobs(userId)
            _jobs.value = jobsList

            // Находим последнюю работу
            val currentJob = jobsList.firstOrNull { it.end == null }
            val lastJob = currentJob ?: jobsList.maxByOrNull { it.start }
            _lastJob.value = lastJob

            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка загрузки работ"
        }
    }

    fun followUser(id: Long) = viewModelScope.launch {
        try {
            userRepository.follow(id)
            loadUser(id) // Обновляем данные пользователя
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при подписке"
        }
    }

    fun unfollowUser(id: Long) = viewModelScope.launch {
        try {
            userRepository.unfollow(id)
            loadUser(id) // Обновляем данные пользователя
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при отписке"
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            loadUsers()
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка обновления"
        }
    }
}