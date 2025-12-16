package ru.netology.nework.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
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
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var avatarUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImage()
        } else {
            Toast.makeText(
                requireContext(),
                "Для выбора фото необходимо разрешение",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
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

        setupTextWatchers()
        setupObservers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearFieldErrors()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.loginEditText.addTextChangedListener(textWatcher)
        binding.nameEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.passwordConfirmEditText.addTextChangedListener(textWatcher)
    }

    private fun clearFieldErrors() {
        binding.loginTextInputLayout.error = null
        binding.nameTextInputLayout.error = null
        binding.passwordTextInputLayout.error = null
        binding.passwordConfirmTextInputLayout.error = null
    }

    private fun setupObservers() {
        viewModel.data.observe(viewLifecycleOwner) { authState ->
            if (authState.isAuthorized) {
                findNavController().popBackStack(R.id.postsFragment, false)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.validationError.observe(viewLifecycleOwner) { error ->
            error?.let { (field, messageResId) ->
                when (field) {
                    "login" -> binding.loginTextInputLayout.error = getString(messageResId)
                    "name" -> binding.nameTextInputLayout.error = getString(messageResId)
                    "password" -> binding.passwordTextInputLayout.error = getString(messageResId)
                    "passwordConfirm" -> binding.passwordConfirmTextInputLayout.error = getString(messageResId)
                    "avatar" -> Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_LONG).show()
                }
                viewModel.clearValidationError()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.registerButton.isEnabled = !isLoading
            binding.avatarButton.isEnabled = !isLoading
            binding.loginEditText.isEnabled = !isLoading
            binding.nameEditText.isEnabled = !isLoading
            binding.passwordEditText.isEnabled = !isLoading
            binding.passwordConfirmEditText.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val login = binding.loginEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val passwordConfirm = binding.passwordConfirmEditText.text.toString()

            if (password != passwordConfirm) {
                binding.passwordConfirmTextInputLayout.error = "Пароли не совпадают"
                return@setOnClickListener
            }

            viewModel.register(login, password, name, avatarUri)
        }

        binding.avatarButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Toast.makeText(
                    requireContext(),
                    "Для выбора фото необходимо разрешение на доступ к хранилищу",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun loadAvatar(uri: Uri) {
        Glide.with(binding.avatarImageView)
            .load(uri)
            .circleCrop()
            .into(binding.avatarImageView)
        binding.avatarImageView.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}