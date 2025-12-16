package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState> = _dataState

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _eventCreated = MutableLiveData(false)
    val eventCreated: LiveData<Boolean> = _eventCreated

    private val _currentEvent = MutableLiveData<Event?>()
    val currentEvent: LiveData<Event?> = _currentEvent

    private val _coordinates = MutableLiveData<Coords?>(null)
    val coordinates: LiveData<Coords?> = _coordinates

    val data: Flow<PagingData<Event>> = repository.data
        .cachedIn(viewModelScope)

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // Paging 3 автоматически загружает данные
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки событий"
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

    fun participate(id: Long) = viewModelScope.launch {
        try {
            repository.participate(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при участии"
        }
    }

    fun unparticipate(id: Long) = viewModelScope.launch {
        try {
            repository.unparticipate(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при отказе от участия"
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _error.value = e.message ?: "Ошибка при удалении события"
        }
    }

    fun createEvent(
        content: String,
        datetime: String,
        type: EventType,
        mediaUpload: MediaUpload? = null,
        link: String? = null,
        speakerIds: List<Long> = emptyList()
    ) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)

            val coords = _coordinates.value

            repository.save(
                content = content,
                datetime = datetime,
                type = type,
                mediaUpload = mediaUpload,
                link = link,
                coords = coords,
                speakerIds = speakerIds
            )

            _eventCreated.value = true
            _coordinates.value = null
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка при создании события"
            _eventCreated.value = false
        }
    }

    fun updateEvent(
        id: Long,
        content: String,
        datetime: String,
        type: EventType,
        mediaUpload: MediaUpload? = null,
        link: String? = null,
        speakerIds: List<Long> = emptyList()
    ) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)

            val coords = _coordinates.value

            repository.update(
                id = id,
                content = content,
                datetime = datetime,
                type = type,
                mediaUpload = mediaUpload,
                link = link,
                coords = coords,
                speakerIds = speakerIds
            )

            _eventCreated.value = true
            _coordinates.value = null
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка при обновлении события"
            _eventCreated.value = false
        }
    }

    fun loadEvent(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            val event = repository.getById(id)
            _currentEvent.value = event
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            _error.value = e.message ?: "Ошибка загрузки события"
        }
    }

    fun setCoordinates(lat: Double, long: Double) {
        _coordinates.value = Coordinates(lat, long)
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