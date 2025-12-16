package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.AppError
import ru.netology.nework.error.asAppError
import ru.netology.nework.model.ProfileModel
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val auth: AppAuth
) : ViewModel() {
    private val _profileState = MutableLiveData(ProfileModel())
    val profileState: LiveData<ProfileModel> = _profileState
    private val _errorState = SingleLiveEvent<AppError>()
    val errorState: LiveData<AppError> = _errorState
    private val _uploadProgress = MutableLiveData<Int?>(null)
    val uploadProgress: LiveData<Int?> = _uploadProgress

    fun loadMyProfile() {
        val userId = auth.authStateFlow.value.userId
        if (userId != 0L) {
            loadProfile(userId, isMyProfile = true)
        }
    }
    fun loadProfile(userId: Long, isMyProfile: Boolean = false) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileModel(loading = true)
                val user = repository.getUserById(userId)
                val jobs = repository.getUserJobs(userId)
                val posts = if (isMyProfile) {
                    repository.getMyWall(0, 20)
                } else {
                    repository.getUserWall(userId, 0, 20)
                }
                _profileState.value = ProfileModel(
                    user = user,
                    jobs = jobs,
                    posts = posts,
                    loading = false,
                    isMyProfile = isMyProfile
                )
            } catch (e: Exception) {
                _profileState.value = ProfileModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun refreshProfile() {
        val currentState = _profileState.value
        if (currentState?.user?.id !=null) {
            loadProfile(currentState.user!!.id, currentState.isMyProfile)
        }
    }
    fun saveJob(job: Job) {
        viewModelScope.launch {
            try {
                val savedJob = repository.saveJOb(job)
                val currentState = _profileState.value
                if (currentState != null) {
                    val updatedJobs = currentState.jobs.toMutableList().apply {
                        add(savedJob)
                    }
                    _profileState.value = currentState.copy(jobs = updatedJobs)
                }
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun deleteJob(jobId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteJob(jobId)
                val currentState = _profileState.value
                if (currentState != null) {
                    val updatedJobs = currentState.jobs.filter { it.id != jobId }
                    _profileState.value = currentState.copy(jobs = updatedJobs)
                }
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun updateAvatar(avatarFile: File) {
        viewModelScope.launch {
            try {
                _uploadProgress.value = 0
                _uploadProgress.value = 50
                val mediaUpload = MediaUpload(id = "temp_avatar")
                val updatedUser = repository.updateAvatar(mediaUpload)
                _uploadProgress.value = 100
                val currentState = _profileState.value
                if (currentState != null) {
                    _profileState.value = currentState.copy(user = updatedUser)
                }
                delay(1000)
                _uploadProgress.value = null
            } catch (e: Exception) {
                _uploadProgress.value = null
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun updateSelectedTab(tab: ProfileTab) {
        val currentState = _profileState.value
        if (currentState != null) {
            _profileState.value = currentState.copy(selectedTab = tab)
        }
    }
    fun loadUserWall(userId: Long) {
        viewModelScope.launch {
            try {
                val posts = repository.getUserWall(userId, 0, 50)
                val currentState = _profileState.value
                if (currentState != null) {
                    _profileState.value = currentState.copy(posts = posts)
                }
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun clearProfile() {
        _profileState.value = ProfileModel()
    }
    fun getCurrentUser(): UserResponse? {
        return _profileState.value?.user
    }
    fun isCurrentUserProfile(): Boolean {
        return _profileState.value?.isMyProfile ?: false
    }
}