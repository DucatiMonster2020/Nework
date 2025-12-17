package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.dto.SelectableUser
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class SelectUsersFragment : Fragment() {

    private lateinit var binding: FragmentSelectUsersBinding
    private val viewModel by viewModels<UserViewModel>()

    private val adapter = UsersAdapter(
        onUserClick = { user ->
            viewModel.toggleUserSelection(user.id)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRecyclerView.adapter = adapter

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    val selectedIds = viewModel.getSelectedUserIds()
                    // Возвращаем результат
                    val result = selectedIds.toLongArray()
                    parentFragmentManager.setFragmentResult(
                        "selected_users",
                        Bundle().apply {
                            putLongArray("user_ids", result)
                        }
                    )
                    findNavController().navigateUp()
                    true
                }
                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest { users ->
                adapter.submitList(users.map { user ->
                    SelectableUser(user, viewModel.isUserSelected(user.id))
                })
                binding.emptyState.isVisible = users.isEmpty()
            }
        }

        binding.emptyState.setOnRefreshListener {
            viewModel.loadUsers()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataState.collectLatest { state ->
                binding.progressBar.isVisible = state.loading
                binding.emptyState.isRefreshing = state.loading
                if (state.error) {
                    // Показать ошибку
                }
            }
        }
    }
}