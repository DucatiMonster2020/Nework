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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.viewmodel.AuthViewModel
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: AuthViewModel by ViewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var avatarUri: Uri? = null
    private var avatarFile: File? = null

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
        uri?.let {
            avatarUri = it
            loadAvatar(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            avatarFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                avatarUri = uri
                loadAvatar(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
        setupInputListeners()
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            register()
        }

        binding.buttonSelectAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.buttonRemoveAvatar.setOnClickListener {
            removeAvatar()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { isAuthenticated ->
                if (isAuthenticated) {
                    findNavController().popBackStack(R.id.postsFragment, false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingState.collectLatest { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.buttonRegister.isEnabled = !isLoading
            }
        }
    }
    private fun setupInputListeners() {
        binding.textInputLogin.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateLogin()
            }
        }

        binding.textInputName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateName()
            }
        }

        binding.textInputPassword.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePassword()
            }
        }

        binding.textInputPasswordConfirm.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePasswordConfirm()
            }
        }
    }

    private fun showAvatarSelectionDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Выберите аватар")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> {}
                }
            }
            .show()
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
        avatarFile = File.createTempFile(
            "avatar_",
            ".jpg",
            requireContext().cacheDir
        )

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            avatarFile!!
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun loadAvatar(uri: Uri) {
        Glide.with(binding.imageViewAvatar)
            .load(uri)
            .circleCrop()
            .into(binding.imageViewAvatar)

        binding.buttonRemoveAvatar.visibility = View.VISIBLE
    }

    private fun removeAvatar() {
        avatarUri = null
        avatarFile = null
        binding.imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        binding.buttonRemoveAvatar.visibility = View.GONE
    }

    private fun register() {
        val login = binding.textInputLogin.editText?.text?.toString()?.trim() ?: ""
        val name = binding.textInputName.editText?.text?.toString()?.trim() ?: ""
        val password = binding.textInputPassword.editText?.text?.toString()?.trim() ?: ""
        val passwordConfirm = binding.textInputPasswordConfirm.editText?.text?.toString()?.trim() ?: ""

        if (!validateLogin() || !validateName() || !validatePassword() || !validatePasswordConfirm()) {
            return
        }

        if (password != passwordConfirm) {
            binding.textInputPasswordConfirm.error = "Пароли не совпадают"
            return
        }

        viewModel.register(login, password, name, avatarFile)
    }

    private fun validateLogin(): Boolean {
        val login = binding.textInputLogin.editText?.text?.toString()?.trim() ?: ""
        return if (login.isBlank()) {
            binding.textInputLogin.error = "Введите логин"
            false
        } else if (login.length < 3) {
            binding.textInputLogin.error = "Логин должен быть не менее 3 символов"
            false
        } else {
            binding.textInputLogin.error = null
            true
        }
    }
    private fun validateName(): Boolean {
        val name = binding.textInputName.editText?.text?.toString()?.trim() ?: ""
        return if (name.isBlank()) {
            binding.textInputName.error = "Введите имя"
            false
        } else {
            binding.textInputName.error = null
            true
        }
    }
    private fun validatePassword(): Boolean {
        val password = binding.textInputPassword.editText?.text?.toString()?.trim() ?: ""
        return if (password.isBlank()) {
            binding.textInputPassword.error = "Введите пароль"
            false
        } else if (password.length < 6) {
            binding.textInputPassword.error = "Пароль должен быть не менее 6 символов"
            false
        } else {
            binding.textInputPassword.error = null
            true
        }
    }
    private fun validatePasswordConfirm(): Boolean {
        val passwordConfirm = binding.textInputPasswordConfirm.editText?.text?.toString()?.trim() ?: ""
        val password = binding.textInputPassword.editText?.text?.toString()?.trim() ?: ""

        return if (passwordConfirm.isBlank()) {
            binding.textInputPasswordConfirm.error = "Подтвердите пароль"
            false
        } else if (passwordConfirm != password) {
            binding.textInputPasswordConfirm.error = "Пароли не совпадают"
            false
        } else {
            binding.textInputPasswordConfirm.error = null
            true
        }
    }
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}