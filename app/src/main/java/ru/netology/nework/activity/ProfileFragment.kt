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
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.viewmodel.ProfileViewModel
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: ProfileViewModel by ViewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var avatarFile: File? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showError("Для смены аватара нужен доступ к камере")
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleAvatarSelection(it) }
    }
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            avatarFile?.let { file ->
                viewModel.updateAvatar(file)
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

        setupViewPager()
        setupClickListeners()
        setupObservers()
        loadProfile()
    }

    private fun setupViewPager() {
        val pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Стена"
                1 -> "Работы"
                else -> ""
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateSelectedTab(
                    when (position) {
                        0 -> ProfileTab.WALL
                        1 -> ProfileTab.JOBS
                        else -> ProfileTab.WALL
                    }
                )
            }
        })
    }
    private fun setupClickListeners() {
        binding.buttonEditAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.buttonAddJob.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_newJobFragment)
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshProfile()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileState.collectLatest { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadProgress.collectLatest { progress ->
                binding.progressBarAvatar.progress = progress ?: 0
                binding.progressBarAvatar.isVisible = progress != null
            }
        }
    }

    private fun loadProfile() {
        viewModel.loadMyProfile()
    }

    private fun updateUI(state: ru.netology.nework.model.ProfileModel) {
        binding.swipeRefresh.isRefreshing = state.loading

        state.user?.let { user ->
            binding.textViewName.text = user.name
            binding.textViewLogin.text = "@${user.login}"

            user.avatar?.let { avatarUrl ->
                Glide.with(binding.imageViewAvatar)
                    .load(avatarUrl)
                    .circleCrop()
                    .into(binding.imageViewAvatar)
            } ?: run {
                binding.imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            }

            state.jobs.firstOrNull { it.finish == null }?.let { currentJob ->
                binding.textViewCurrentJob.text = "${currentJob.position} в ${currentJob.name}"
            } ?: run {
                binding.textViewCurrentJob.text = "В поиске работы"
            }
        }
        binding.buttonAddJob.isVisible = state.isMyProfile && binding.viewPager.currentItem == 1
    }

    private fun showAvatarSelectionDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Сменить аватар")
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

    private fun handleAvatarSelection(uri: Uri) {
        showError("Выбрано изображение из галереи")
    }

    private fun logout() {
        appAuth.removeAuth()
        findNavController().popBackStack(R.id.postsFragment, false)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
