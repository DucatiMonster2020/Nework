package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UserProfilePagerAdapter
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
    private val args: UserProfileFragmentArgs by navArgs()

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: UserProfilePagerAdapter

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
        observeViewModel()
        loadUserData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = UserProfilePagerAdapter(this, args.userId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_wall)
                1 -> getString(R.string.tab_jobs)
                else -> ""
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Можно добавить логику при смене таба
            }
        })
    }
    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { bindUser(it) }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Показать ошибку
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            binding.progress.isVisible = true
            try {
                viewModel.loadUser(args.userId)
            } catch (e: Exception) {
                // Обработка ошибки
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    private fun bindUser(user: User) {
        binding.apply {
            userName.text = user.name
            userLogin.text = "@${user.login}"

            // Аватар
            user.avatar?.let { avatarUrl ->
                Glide.with(userAvatar)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(userAvatar)
            } ?: run {
                userAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            }

            // Координаты (если есть)
            locationContainer.isVisible = user.lat != null && user.lng != null
            user.lat?.let { lat ->
                user.lng?.let { lng ->
                    showOnMap.setOnClickListener {
                        findNavController().navigate(
                            UserProfileFragmentDirections.actionUserProfileFragmentToMapFragment(
                                lat = lat,
                                lng = lng
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}