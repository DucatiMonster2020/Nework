package ru.netology.nework.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils
import ru.netology.nework.viewmodel.EventViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: EventViewModel by viewModels()
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!
    private var mediaUri: Uri? = null
    private var mediaType: String? = null
    private var eventDateTime: LocalDateTime? = null
    private val selectedSpeakerIds = mutableListOf<Long>()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showAttachmentTypeDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Необходимы разрешения для доступа к файлам",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                mediaUri = uri
                mediaType = getMediaTypeFromUri(uri)
                loadMediaPreview(uri)
            }
        }
    }
    private val pickLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getDoubleExtra("latitude", 0.0)?.let { lat ->
                result.data?.getDoubleExtra("longitude", 0.0)?.let { lon ->
                    viewModel.setCoordinates(lat, lon)
                    binding.locationButton.text = getString(R.string.location_selected)
                }
            }
        }
    }
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

        setupToolbar()
        setupObservers()
        setupClickListeners()
        setupDateTime()
    }
    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.save -> {
                        saveEvent()
                        true
                    } else -> false
                }
            }
        }
    }
    private fun setupObservers() {
        viewModel.eventCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().popBackStack()
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun setupClickListeners() {
        binding.attachmentButton.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
        binding.locationButton.setOnClickListener {
            val action = NewEventFragmentDirections.actionNewEventFragmentToMapFragment()
            pickLocationLauncher.launch(Intent(requireContext(), MapFragment::class.java).apply {
                putExtras(action.arguments)
            })
        }
        binding.speakersButton.setOnClickListener {
            selectSpeakers()
        }
        binding.dateTimeButton.setOnClickListener {
            showDateTimePicker()
        }
    }
    private fun selectSpeakers() {
        val action = NewEventFragmentDirections.actionNewEventFragmentToUsersFrament(
            selectMode = true
        )
        speakerSelectionLauncher.launch(action)
    }
    private val speakerSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getLongArrayExtra("selected_user_ids")?.let { ids ->
                selectedSpeakerIds.clear()
                selectedSpeakerIds.addAll(ids.toList())
                updateSpeakersButton()
            }
        }
    }
    private fun updateSpeakersButton() {
        val count = selectedSpeakerIds.size
        binding.speakersButton.text = if (count > 0) "Спикеры: $count"
        else "Добавить спикеров"
    }
    private fun setupDateTime() {
        eventDateTime = LocalDateTime.now().plusDays(1)
        updateDateTimeButton()
    }
    private fun checkPermissionsAndShowDialog() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            showAttachmentTypeDialog()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }
    private fun showAttachmentTypeDialog() {
        val items = arrayOf(
            getString(R.string.attachment_photo),
            getString(R.string.attachment_audio),
            getString(R.string.attachment_video)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_attachment)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> pickImage()
                    1 -> pickAudio()
                    2 -> pickVideo()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    private fun pickImage() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        pickMediaLauncher.launch(intent)
    }
    private fun pickAudio() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickMediaLauncher.launch(intent)
    }
    private fun pickVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            type = "video/*"
        }
        pickMediaLauncher.launch(intent)
    }
    private fun getMediaTypeFromUri(uri: Uri): String? {
        return context?.contentResolver?.getType(uri)
    }
    private fun loadMediaPreview(uri: Uri) {
        when {
            mediaType?.startsWith("image/") == true -> {
                binding.mediaPreview.isVisible = true
                binding.audioPreview.isVisible = false
                binding.videoPreview.isVisible = false
                Glide.with(binding.mediaPreview)
                    .load(uri)
                    .into(binding.mediaPreview)
            }
            mediaType?.startsWith("audio/") == true -> {
                binding.mediaPreview.isVisible = false
                binding.audioPreview.isVisible = true
                binding.videoPreview.isVisible = false
            }
            mediaType?.startsWith("video/") == true -> {
                binding.mediaPreview.isVisible = true
                binding.audioPreview.isVisible = false
                binding.videoPreview.isVisible = true
                Glide.with(binding.mediaPreview)
                    .load(uri)
                    .into(binding.mediaPreview)
            }
        }
        binding.removeAttachmentButton.isVisible = true
        binding.removeAttachmentButton.setOnClickListener {
            mediaUri = null
            mediaType = null
            binding.mediaPreview.isVisible = false
            binding.audioPreview.isVisible = false
            binding.videoPreview.isVisible = false
            binding.removeAttachmentButton.isVisible = false
        }
    }
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        eventDateTime = LocalDateTime.of(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay,
                            hourOfDay,
                            minute
                        )
                        updateDateTimeButton()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            year,
            month,
            day
        ).show()
    }
    private fun updateDateTimeButton() {
        eventDateTime?.let { dateTime ->
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            binding.dateTimeButton.text = dateTime.format(formatter)
        }
    }
    private fun saveEvent() {
        val content = binding.contentEditText.text.toString()

        if (content.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Содержание события не может быть пустым",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (eventDateTime == null) {
            Toast.makeText(
                requireContext(),
                "Выберите дату и время проведения события",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val eventType = if (binding.onlineRadioButton.isChecked) {
            EventType.ONLINE
        } else {
            EventType.OFFLINE
        }
        val mediaUpload = mediaUri?.let { uri ->
            val filePath = AndroidUtils.getFilePathFromUri(uri)

            val validation = ValidationUtils.validateMediaFile(filePath)
            if (validation is ValidationUtils.ValidationResult.Error) {
                Toast.makeText(requireContext(), getString(validation.messageResId), Toast.LENGTH_LONG).show()
                return
            }

            File(filePath).let { file ->
                MediaUpload(file)
            }
        }
        viewModel.createEvent(
            content = content,
            datetime = eventDateTime!!.toString(),
            type = eventType,
            mediaUpload = mediaUpload,
            link = null,
            speakerIds = selectedSpeakerIds
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}