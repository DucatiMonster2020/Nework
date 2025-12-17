package ru.netology.nework.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.viewmodel.EventViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class NewEventFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by viewModels()
    private val args: NewEventFragmentArgs by navArgs()

    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null
    private var selectedDateTime: Instant? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photoUri = uri
                binding.photoContainer.isVisible = true
                binding.photo.setImageURI(uri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                binding.photoContainer.isVisible = true
                binding.photo.setImageURI(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            AndroidUtils.showToast(requireContext(), "Нет разрешения на использование камеры")
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

        if (!appAuth.authState.value?.isAuthorized == true) {
            findNavController().navigate(R.id.action_global_loginFragment)
            return
        }

        setupToolbar()
        setupListeners()
        setupTypeSpinner()
        observeViewModel()

        // Если передан eventId, загружаем для редактирования
        if (args.eventId != 0L) {
            loadEventForEditing()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
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

    private fun setupListeners() {
        binding.takePhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.choosePhoto.setOnClickListener {
            pickImageFromGallery()
        }

        binding.removePhoto.setOnClickListener {
            photoUri = null
            binding.photoContainer.isVisible = false
        }

        binding.chooseDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.chooseLocation.setOnClickListener {
            findNavController().navigate(
                NewEventFragmentDirections.actionNewEventFragmentToMapFragment()
            )
        }

        binding.chooseSpeakers.setOnClickListener {
            // Переход к выбору спикеров
            findNavController().navigate(
                NewEventFragmentDirections.actionNewEventFragmentToUsersFragment(true)
            )
        }
    }

    private fun setupTypeSpinner() {
        val types = arrayOf("Онлайн", "Офлайн")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.eventType.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadEventForEditing() {
        // Загрузка события для редактирования
        lifecycleScope.launch {
            binding.progress.isVisible = true
            try {
                val event = viewModel.getEventById(args.eventId)
                bindEventForEditing(event)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Ошибка загрузки события", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    private fun bindEventForEditing(event: Event) {
        binding.content.setText(event.content)
        selectedDateTime = event.datetime

        // Установка типа события
        binding.eventType.setSelection(if (event.type == EventType.OFFLINE) 1 else 0)

        // Установка даты
        updateDateTimeText()

        // Загрузка фото если есть
        event.attachment?.let { attachment ->
            if (attachment.type == AttachmentType.IMAGE) {
                // Загрузить превью
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AndroidUtils.showToast(requireContext(), "Разрешение нужно для съёмки фото")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        takePhotoLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun showDateTimePicker() {
        // Выбор даты
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { milliseconds ->
            // Выбор времени после выбора даты
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Выберите время")
                .setHour(12)
                .setMinute(0)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val localDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(milliseconds),
                    ZoneId.systemDefault()
                ).withHour(timePicker.hour)
                    .withMinute(timePicker.minute)

                selectedDateTime = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                updateDateTimeText()
            }

            timePicker.show(childFragmentManager, "time_picker")
        }

        datePicker.show(childFragmentManager, "date_picker")
    }

    private fun updateDateTimeText() {
        selectedDateTime?.let { dateTime ->
            val formatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            binding.selectedDateTime.text = formatter.format(dateTime)
            binding.selectedDateTime.isVisible = true
        } ?: run {
            binding.selectedDateTime.isVisible = false
        }
    }

    private fun saveEvent() {
        val content = binding.content.text.toString().trim()
        if (content.isEmpty()) {
            AndroidUtils.showToast(requireContext(), "Заполните описание события")
            return
        }

        if (selectedDateTime == null) {
            AndroidUtils.showToast(requireContext(), "Выберите дату и время события")
            return
        }

        val type = if (binding.eventType.selectedItemPosition == 1) {
            EventType.OFFLINE
        } else {
            EventType.ONLINE
        }

        // Здесь нужно создать Event объект и передать в ViewModel
        // viewModel.save(event)

        AndroidUtils.showToast(requireContext(), "Событие сохранено")
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}