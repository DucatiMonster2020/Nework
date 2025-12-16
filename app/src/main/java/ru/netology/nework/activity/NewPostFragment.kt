package ru.netology.nework.activity

import android.Manifest
import android.app.Activity
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
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.FileUtils
import ru.netology.nework.util.ValidationUtils
import ru.netology.nework.viewmodel.PostViewModel
import java.io.File

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private var mediaUri: Uri? = null
    private var mediaType: String? = null
    private var selectedMentionIds = mutableListOf<Long>()

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
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.save -> {
                        savePost()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.postCreated.observe(viewLifecycleOwner) { success ->
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
            val action = NewPostFragmentDirections.actionNewPostFragmentToMapFragment()
            pickLocationLauncher.launch(Intent(requireContext(), MapFragment::class.java).apply {
                putExtras(action.arguments)
            })
        }

        binding.mentionButton.setOnClickListener {
            selectMentions()
        }
    }
    private fun selectMentions() {
        val action = NewPostFragmentDirections.actionNewPostFragmentToUsersFragment(
            selectMode = true
        )
        mentionSelectionLauncher.launch(action)
    }
    private val mentionSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getLongArrayExtra("selected_user_ids")?.let { ids ->
                selectedMentionIds.clear()
                selectedMentionIds.addAll(ids.toList())
                updateMentionButton()
            }
        }
    }
    private fun updateMentionButton() {
        val count = selectedMentionIds.size
        binding.mentionButton.text = if (count > 0) "Упомянуто: $count"
        else "Упомянуть пользователя"
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
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery),
            getString(R.string.choose_audio),
            getString(R.string.choose_video)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_attachment)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> takePhoto() // Сделать фото
                    1 -> pickImage() // Выбрать из галереи
                    2 -> pickAudio()
                    3 -> pickVideo()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun takePhoto() {
        if (!FileUtils.isCameraAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                "Камера не доступна на этом устройстве",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(requireContext(), CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                mediaUri = uri
                loadMediaPreview(uri)
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
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
                // Настройка аудиоплеера
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
    }

    private fun savePost() {
        val content = binding.contentEditText.text.toString()

        if (content.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Содержание поста не может быть пустым",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val mediaUpload = mediaUri?.let { uri ->
            val erroorMessage = FileUtils.getFileSizeErrorMessage(requireContext(), uri)
            if (erroorMessage != null) {
                Toast.makeText(requireContext(), erroorMessage, Toast.LENGTH_LONG).show()
                return
            }
            val filePath = AndroidUtils.getFilePathFromUri(uri)
            File(filePath).let { file ->
                MediaUpload(file)
            }
        }

        viewModel.createPost(
            content = content,
            mediaUpload = mediaUpload,
            link = null,
            mentionIds = selectedMentionIds
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}