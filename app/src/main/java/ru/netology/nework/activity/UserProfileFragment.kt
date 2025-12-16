package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.model.ProfileTab
import ru.netology.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val args: UserProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupObservers()
        loadProfile()
    }

    private fun setupViewPager() {
        val pagerAdapter = ProfilePagerAdapter(this, isMyProfile = false)
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
    }

    private fun loadProfile() {
        viewModel.loadUserById(args.userId)
    }

    private fun updateUI(state: ru.netology.nework.model.ProfileModel) {
        state.user?.let { user ->
            binding.toolbar.title = user.name
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

        binding.progressBar.isVisible = state.loading
        binding.swipeRefresh.isRefreshing = state.refreshing
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}