package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.viewmodel.UserViewModel
import kotlin.getValue

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupListeners()
        observeViewModel()
        loadUsers()
    }
    private fun setupAdapter() {
        adapter = UsersAdapter(
            onItemClickListener = { user ->
                findNavController().navigate(
                    R.id.action_usersFragment_to_userProfileFragment,
                    Bundle().apply {
                        putLong("userId", user.id)
                    }
                )
            }
        )

        binding.usersList.layoutManager = LinearLayoutManager(requireContext())
        binding.usersList.adapter = adapter
        binding.usersList.setHasFixedSize(true)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadUsers()
        }

        binding.retryButton.setOnClickListener {
            loadUsers()
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            binding.progress.isVisible = true
            binding.errorGroup.isVisible = false

            try {
                val users = viewModel.loadUsers()
                adapter.submitList(users)
                binding.emptyState.isVisible = users.isEmpty()
            } catch (e: Exception) {
                binding.errorGroup.isVisible = true
                Snackbar.make(binding.root, "Ошибка загрузки пользователей", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progress.isVisible = false
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}