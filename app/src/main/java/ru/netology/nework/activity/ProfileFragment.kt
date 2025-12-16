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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils
import ru.netology.nework.viewmodel.ProfileViewModel
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
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
                "Для изменения фото необходимо разрешение",
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
                uploadAvatar(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
        setupObservers()
        setupClickListeners()

        viewModel.loadProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            inflateMenu(R.menu.profile_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.addJob -> {
                        showAddJobDialog()
                        true
                    }
                    R.id.editProfile -> {
                        // TODO: Реализовать редактирование профиля
                        true
                    }
                    else -> false
                }
            }
        }
    }
    private fun setupViewPager() {
        val pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.wall)
                1 -> getString(R.string.jobs)
                else -> ""
            }
        }.attach()
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { user ->
            user?.let {
                bindProfile(it)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.avatarImageView.setOnClickListener {
            checkPermissionAndPickImage()
        }
    }

    private fun bindProfile(user: ru.netology.nework.dto.User) {
        binding.userName.text = user.name
        binding.userLogin.text = "@${user.login}"

        Glide.with(binding.avatarImageView)
            .load(user.avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.avatarImageView)

        // Последнее место работы
        viewModel.lastJob.observe(viewLifecycleOwner) { job ->
            binding.lastJob.text = job?.let {
                "${it.position} в ${it.name}"
            } ?: getString(R.string.looking_for_job)
            binding.lastJob.isVisible = true
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
                    "Для изменения фото необходимо разрешение на доступ к хранилищу",
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
    }

    private fun uploadAvatar(uri: Uri) {
        val filePath = AndroidUtils.getFilePathFromUri(uri)

        val validation = ValidationUtils.validateAvatar(filePath)
        if (validation is ValidationUtils.ValidationResult.Error) {
            Toast.makeText(requireContext(), getString(validation.messageResId), Toast.LENGTH_LONG).show()
            return
        }

        viewModel.uploadAvatar(uri)
    }

    private fun showAddJobDialog() {
        val dialog = AddJobDialogFragment { company, position, startDate, endDate, isCurrent ->
            viewModel.addJob(company, position, startDate, if (isCurrent) null else endDate)
        }
        dialog.show(parentFragmentManager, "AddJobDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}