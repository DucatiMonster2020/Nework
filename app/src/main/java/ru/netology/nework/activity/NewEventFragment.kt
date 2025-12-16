package ru.netology.nework.activity

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.EventRequest
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.viewmodel.EventViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by viewModels()
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    private var selectedDateTime: Instant? = null
    private var selectedCoordinates: Coordinates? = null
    private var selectedAttachment: Attachment? = null
    private var selectedSpeakerIds = emptyList<Long>()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showError("Для добавления фото нужен доступ к камере")
        }
    }
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleImageUri(it) }
    }
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraFile?.let { file ->
                handleCameraImage(file)
            }
        }
    }

    private var cameraFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
        setupToolbar()

        val eventId = arguments?.getLong("eventId")
        if (eventId != null && eventId != 0L) {
            loadEvent(eventId)
            binding.toolbar.title = "Редактировать событие"
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    saveEvent()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }
        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioOnline -> viewModel.updateType(EventType.ONLINE)
                R.id.radioOffline -> viewModel.updateType(EventType.OFFLINE)
            }
        }
        binding.buttonSelectLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_mapFragment)
        }
        binding.buttonSelectSpeakers.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_usersFragment)
        }
        binding.buttonTakePhoto.setOnClickListener {
            checkCameraPermission()
        }
        binding.buttonSelectFromGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.buttonSelectAudio.setOnClickListener {
            selectAudioFile()
        }
        binding.buttonSelectVideo.setOnClickListener {
            selectVideoFile()
        }
        binding.buttonRemoveAttachment.setOnClickListener {
            selectedAttachment = null
            updateAttachmentUI()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventContentState.collectLatest { content ->
                binding.editTextContent.setText(content)
                selectedDateTime = content.datetime
                selectedCoordinates = content.coordinates
                selectedAttachment = content.attachment
                selectedSpeakerIds = content.speakerIds

                updateDateTimeUI()
                updateLocationUI()
                updateAttachmentUI()
                updateSpeakersUI()
                updateEventTypeUI(content.type)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }
    }

    private fun loadEvent(eventId: Long) {
        viewModel.loadEventById(eventId)
    }

    private fun saveEvent() {
        val content = binding.editTextContent.text.toString().trim()

        if (content.isBlank()) {
            showError("Введите описание события")
            return
        }

        if (selectedDateTime == null) {
            showError("Выберите дату и время проведения")
            return
        }

        val event = EventRequest(
            id = arguments?.getLong("eventId") ?: 0L,
            content = content,
            datetime = selectedDateTime,
            type = if (binding.radioOnline.isChecked) EventType.ONLINE else EventType.OFFLINE,
            coords = selectedCoordinates,
            attachment = selectedAttachment?.let {
                MediaUpload(it.url)
            },
            link = null,
            speakerIds = selectedSpeakerIds
        )

        viewModel.save(event)
        findNavController().popBackStack()
    }

    private fun showDateTimePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Выберите время")
                .setHour(12)
                .setMinute(0)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val date = Date(millis)
                val localDateTime = LocalDateTime.ofInstant(
                    date.toInstant(),
                    ZoneId.systemDefault()
                )
                    .withHour(timePicker.hour)
                    .withMinute(timePicker.minute)

                selectedDateTime = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                viewModel.updateDatetime(selectedDateTime)
                updateDateTimeUI()
            }

            timePicker.show(parentFragmentManager, "time_picker")
        }

        datePicker.show(parentFragmentManager, "date_picker")
    }

    private fun updateDateTimeUI() {
        binding.textViewDateTime.text = selectedDateTime?.let {
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            formatter.format(Date.from(it))
        } ?: "Не выбрано"
    }

    private fun updateLocationUI() {
        binding.textViewLocation.text = selectedCoordinates?.let {
            "Широта: ${it.lat}, Долгота: ${it.long}"
        } ?: "Не выбрано"
    }

    private fun updateAttachmentUI() {
        binding.apply {
            imageViewAttachmentPreview.isVisible = selectedAttachment != null
            buttonRemoveAttachment.isVisible = selectedAttachment != null

            selectedAttachment?.let { attachment ->
                when (attachment.type) {
                    ru.netology.nework.enumeration.AttachmentType.IMAGE -> {
                        Glide
                            .with(imageViewAttachmentPreview)
                            .load(attachment.url)
                            .into(imageViewAttachmentPreview)
                    }
                    ru.netology.nework.enumeration.AttachmentType.AUDIO -> {
                        imageViewAttachmentPreview.setImageResource(R.drawable.ic_audio)
                    }
                    ru.netology.nework.enumeration.AttachmentType.VIDEO -> {
                        imageViewAttachmentPreview.setImageResource(R.drawable.ic_video)
                    }
                }
            }
        }
    }

    private fun updateSpeakersUI() {
        binding.textViewSpeakers.text = if (selectedSpeakerIds.isNotEmpty()) {
            "Выбрано: ${selectedSpeakerIds.size}"
        } else {
            "Не выбраны"
        }
    }

    private fun updateEventTypeUI(type: EventType) {
        when (type) {
            EventType.ONLINE -> binding.radioOnline.isChecked = true
            EventType.OFFLINE -> binding.radioOffline.isChecked = true
        }
    }

    private fun checkCameraPermission() {
        val permission = CAMERA
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraFile = File.createTempFile(
            "event_photo_",
            ".jpg",
            requireContext().cacheDir
        )

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            cameraFile!!
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        cameraLauncher.launch(intent)
    }

    private fun handleImageUri(uri: Uri) {
        showError("Выбрано изображение из галереи")
    }

    private fun handleCameraImage(file: File) {
        showError("Сделано фото")
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, AUDIO_REQUEST_CODE)
    }

    private fun selectVideoFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, VIDEO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AUDIO_REQUEST_CODE -> {
                    showError("Выбран аудиофайл")
                }
                VIDEO_REQUEST_CODE -> {
                    showError("Выбран видеофайл")
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val AUDIO_REQUEST_CODE = 1001
        private const val VIDEO_REQUEST_CODE = 1002
    }
}