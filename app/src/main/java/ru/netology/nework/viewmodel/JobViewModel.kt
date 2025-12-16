package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.internal.StatusExceptionMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.AppError
import ru.netology.nework.error.asAppError
import ru.netology.nework.model.JobModel
import ru.netology.nework.model.ProfileModel
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    private val _jobState = MutableLiveData(JobModel())
    val jobState: LiveData<JobModel> = _jobState
    private val _errorState = SingleLiveEvent<AppError>()
    val errorState: LiveData<AppError> = _errorState
    private val _saveSuccess = SingleLiveEvent<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadJob(jobId: Long) {
        viewModelScope.launch {
            try {
                _jobState.value = JobModel(loading = true)
                delay(300)
                _jobState.value = JobModel(
                    loading = false
                )
            } catch (e: Exception) {
                _jobState.value = JobModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun createNewJob() {
        _jobState.value = JobModel(
            job = Job(
                id = 0,
                name = "",
                position = "",
                start = Instant.now(),
                finish = null,
                link = null
            ),
            isEditing = true
        )
    }
    fun editJob(job: Job) {
        _jobState.value = JobModel(
            job = job.copy(),
            isEditing = true
        )
    }
    fun saveJob(
        name: String,
        position: String,
        start: Instant,
        finish: Instant?,
        link: String?
    ) {
        if (name.isBlank() || position.isBlank()) {
            _errorState.postValue(AppError.ValidationError("Поля", "Название и должность обязательны"))
            return
        }
        if (finish !=null && finish.isBefore(start)) {
            _errorState.postValue(AppError.ValidationError("Даты", "Дата окончания не может быть раньше начала"))
            return
        }
        viewModelScope.launch {
            try {
                _jobState.value = JobModel(loading = true)
                val currentJob = _jobState.value?.job
                val jobToSave = Job(
                    id = currentJob?.id ?: 0,
                    name = name.trim(),
                    position = position.trim(),
                    start = start,
                    finish = finish,
                    link = link?.takeIf { it.isNotBlank() }
                )
                val savedJob = repository.saveJOb(jobToSave)
                _jobState.value = JobModel(
                    job = savedJob,
                    loading = false
                )
                _saveSuccess.postValue(true)
            } catch (e: Exception) {
                _jobState.value = JobModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun deleteJob(jobId: Long) {
        viewModelScope.launch {
            try {
                _jobState.value = JobModel(loading = true)
                repository.deleteJob(jobId)
                _jobState.value = JobModel(loading = false)
                _saveSuccess.postValue(true)
            } catch (e: Exception) {
                _jobState.value = JobModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun updateJobName(name: String) {
        val currentState = _jobState.value
        if (currentState?.job != null) {
            _jobState.value = currentState.copy(
                job = currentState.job!!.copy(name = name)
            )
        }
    }
    fun updateJobPosition(position: String) {
        val currentState = _jobState.value
        if (currentState?.job != null) {
            _jobState.value = currentState.copy(
                job = currentState.job!!.copy(position = position)
            )
        }
    }
    fun updateJobStart(start: Instant) {
        val currentState = _jobState.value
        if (currentState?.job != null) {
            _jobState.value = currentState.copy(
                job = currentState.job!!.copy(start = start)
            )
        }
    }
    fun updateJobFinish(finish: Instant) {
        val currentState = _jobState.value
        if (currentState?.job != null) {
            _jobState.value = currentState.copy(job = currentState.job!!.copy(finish = finish)
            )
        }
    }
    fun updateJobLink(link: String?) {
        val currentState = _jobState.value
        if (currentState?.job != null) {
            _jobState.value = currentState.copy(job = currentState.job!!.copy(link = link)
            )
        }
    }

    fun clearJob() {
        _jobState.value = JobModel()
    }
    fun cancelEditing() {
        val currentState = _jobState.value
        if (currentState != null) {
            _jobState.value = currentState.copy(isEditing = false, job = null)
        }
    }
}