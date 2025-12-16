package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.error.AppError
import ru.netology.nework.error.asAppError
import ru.netology.nework.model.EventContentModel
import ru.netology.nework.model.EventModel
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.util.SingleLiveEvent
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val auth: AppAuth
) : ViewModel() {
    private val _eventState = MutableLiveData(EventModel())
    val eventState: LiveData<EventModel> = _eventState
    private val _eventContentState = MutableLiveData(EventContentModel())
    val eventContentState: LiveData<EventContentModel> = _eventContentState
    private val _errorState = SingleLiveEvent<AppError>()
    val errorState: LiveData<AppError> = _errorState

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Event>> = auth.authStateFlow
        .flatMapLatest { (token, _) ->
            repository.data.map { pagingData ->
                pagingData.map { event ->
                    event.copy(ownedByMe = event.authorId == auth.authStateFlow.value.userId)
                }
            }
        }
    fun loadEvents() {
        viewModelScope.launch {
            try {
                _eventState.value = EventModel(loading = true)
                repository.getAll()
                _eventState.value = EventModel(loading = false)
            } catch (e: Exception) {
                _eventState.value = EventModel(loading = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun refresh() {
        viewModelScope.launch {
            try {
                _eventState.value = EventModel(refreshing = true)
                repository.getAll()
                _eventState.value = EventModel(refreshing = false)
            } catch (e: Exception) {
                _eventState.value = EventModel(refreshing = false, error = e.asAppError())
                _errorState.postValue(e.asAppError())
            }
        }
    }

    fun save(event: EventRequest) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.save(event)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun removeBiId(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
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
    fun participateById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.participateById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun unparticipateById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.unparticipateById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
    fun updateContent(content: String) {
        _eventContentState.value = _eventContentState.value?.copy(content = content)
    }
    fun updateDatetime(datetime: Instant?) {
        _eventContentState.value = _eventContentState.value?.copy(datetime = datetime)
    }
    fun updateType(type: EventType) {
        _eventContentState.value = _eventContentState.value?.copy(type = type)
    }
    fun updateCoordinates(coords: Coordinates?) {
        _eventContentState.value = _eventContentState.value?.copy(coordinates = coords)
    }
    fun updateLink(link: String?) {
        _eventContentState.value = _eventContentState.value?.copy(link = link)
    }
    fun updateSpeakerIds(speakerIds: List<Long>) {
        _eventContentState.value = _eventContentState.value?.copy(speakerIds = speakerIds)
    }
    fun clearContent() {
        _eventContentState.value = EventContentModel()
    }
    fun loadEventById(id: Long) {
        viewModelScope.launch {
            try {
                val event = repository.getEventById(id)
            } catch (e: Exception) {
                _errorState.postValue(e.asAppError())
            }
        }
    }
}