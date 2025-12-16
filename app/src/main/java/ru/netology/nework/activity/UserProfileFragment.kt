package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private val args: UserProfileFragmentArgs by navArgs()

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

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

        setupToolbar()
        setupViewPager()
        setupObservers()

        viewModel.loadUser(args.userId)
        viewModel.loadUserJobs(args.userId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.follow -> {
                        viewModel.followUser(args.userId)
                        true
                    }
                    R.id.unfollow -> {
                        viewModel.unfollowUser(args.userId)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = UserProfilePagerAdapter(this, args.userId)
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
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                bindUser(it)
            }
        }

        viewModel.lastJob.observe(viewLifecycleOwner) { job ->
            binding.lastJob.text = job?.let {
                "${it.position} в ${it.name}"
            } ?: getString(R.string.looking_for_job)
            binding.lastJob.isVisible = true
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                // TODO: Показать ошибку
            }
        }
    }

    private fun bindUser(user: ru.netology.nework.dto.User) {
        binding.userName.text = user.name
        binding.userLogin.text = "@${user.login}"

        Glide.with(binding.avatarImageView)
            .load(user.avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.avatarImageView)

        // Настройка меню подписки
        val isFollowing = false // TODO: Получить из API
        binding.toolbar.menu.findItem(R.id.follow).isVisible = !isFollowing
        binding.toolbar.menu.findItem(R.id.unfollow).isVisible = isFollowing
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}