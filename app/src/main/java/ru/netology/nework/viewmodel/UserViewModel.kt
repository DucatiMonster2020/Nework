package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.error.AppError
import ru.netology.nework.error.asAppError
import ru.netology.nework.model.ProfileModel
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.model.UserModel
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val auth: AppAuth
): ViewModel() {
    private val _userState = MutableLiveData(UserModel())
    val userState: LiveData<UserModel> = _userState
    private val _profileState = MutableLiveData(ProfileModel())
    val profileState: LiveData<ProfileModel> = _profileState
    private val _errorState = SingleLiveEvent<AppError>()
    val errorState: LiveData<AppError> = _errorState

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _userState.value = UserModel(loading = true)
                val users = repository.getAll()
                _userState.value = UserModel(users = users, loading = false)
            } catch (e: Exception) {
                _userState.value = UserModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun loadUserById(id: Long) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileModel(loading = true)
                val user = repository.getUserById(id)
                val jobs = repository.getUserJobs(id)
                val posts = repository.getUserWall(id, 0, 20)
                _profileState.value = ProfileModel(
                    user = user,
                    jobs = jobs,
                    posts = posts,
                    loading = false,
                    isMyProfile = id == auth.authStateFlow.value.userId
                )
            } catch (e: Exception) {
                _profileState.value = ProfileModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun loadMyProfile() {
        val userId = auth.authStateFlow.value.userId
        if (userId != 0L) {
            loadUserById(userId)
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
    fun deleteJob(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteJob(id)
                val currentState = _profileState.value
                if (currentState != null) {
                    val updatedJobs = currentState.jobs.filter { it.id != id }
                    _profileState.value = currentState.copy(jobs = updatedJobs)
                }
            } catch (e: Exception) {
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
    fun searchUsers(query: String) {
        val currentState = _userState.value
        if (currentState != null) {
            val filteredUsers = if (query.isBlank()) {
                currentState.users
            } else {
                currentState.users.filter { user ->
                    user.login.contains(query, ignoreCase = true) ||
                            user.name.contains(query, ignoreCase = true)
                }
            }
            _userState.value = currentState.copy(searchQuery = query)
        }
    }
    fun clearProfile() {
        _profileState.value = ProfileModel()
    }
}