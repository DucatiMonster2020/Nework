package ru.netology.nework.viewmodel

import android.net.Uri
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState> = _dataState

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _profile = MutableLiveData<User?>()
    val profile: LiveData<User?> = _profile

    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs

    private val _lastJob = MutableLiveData<Job?>()
    val lastJob: LiveData<Job?> = _lastJob

    val wallPosts: Flow<PagingData<Post>> = postRepository.wallData
        .cachedIn(viewModelScope)

    fun loadProfile() = viewModelScope.launch {
        try {
            _loading.value = true
            val user = userRepository.getMyProfile()
            _profile.value = user
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка загрузки профиля"
        }
    }

    fun loadWall() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // Paging 3 автоматически загружает данные
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки стены"
        }
    }

    fun loadJobs() = viewModelScope.launch {
        try {
            _loading.value = true
            val jobsList = userRepository.getMyJobs()
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

    fun addJob(
        company: String,
        position: String,
        startDate: String,
        endDate: String?
    ) = viewModelScope.launch {
        try {
            _loading.value = true
            userRepository.saveJob(company, position, startDate, endDate)
            loadJobs() // Обновляем список
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка при добавлении работы"
        }
    }

    fun updateJob(
        id: Long,
        company: String,
        position: String,
        startDate: String,
        endDate: String?
    ) = viewModelScope.launch {
        try {
            _loading.value = true
            userRepository.updateJob(id, company, position, startDate, endDate)
            loadJobs() // Обновляем список
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка при обновлении работы"
        }
    }

    fun deleteJob(id: Long) = viewModelScope.launch {
        try {
            _loading.value = true
            userRepository.removeJob(id)
            loadJobs() // Обновляем список
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка при удалении работы"
        }
    }

    fun uploadAvatar(uri: Uri) = viewModelScope.launch {
        try {
            _loading.value = true
            userRepository.updateAvatar(uri)
            loadProfile() // Обновляем профиль
            _loading.value = false
        } catch (e: Exception) {
            _loading.value = false
            _error.value = e.message ?: "Ошибка при обновлении аватара"
        }
    }

    fun likePost(id: Long) = viewModelScope.launch {
        try {
            postRepository.likeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при лайке"
        }
    }

    fun deletePost(id: Long) = viewModelScope.launch {
        try {
            postRepository.removeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при удалении поста"
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            loadProfile()
            loadJobs()
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка обновления"
        }
    }
}

