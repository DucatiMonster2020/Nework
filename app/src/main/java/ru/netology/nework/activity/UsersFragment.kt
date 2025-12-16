package ru.netology.nework.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
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

    private var selectMode = false
    private val selectedUsers = mutableSetOf<Long>()
    private lateinit var adapter: UsersAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectMode = UsersFragmentArgs.fromBundle(it).selectMode
        }
    }

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

        setupToolbar()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                if (selectMode) {
                    returnSelectedUsers()
                } else {
                    findNavController().navigateUp()
                }
            }

            if (selectMode) {
                inflateMenu(R.menu.select_users_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.done -> {
                            returnSelectedUsers()
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = UsersAdapter(object : UsersAdapter.UserInteractionListener {
            override fun onUserClick(userId: Long) {
                if (selectMode) {
                    if (selectedUsers.contains(userId)) {
                        selectedUsers.remove(userId)
                    } else {
                        selectedUsers.add(userId)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    val action = UsersFragmentDirections
                        .actionUsersFragmentToUserProfileFragment(userId)
                    findNavController().navigate(action)
                }
            }

            override fun onUserSelected(userId: Long, selected: Boolean) {
                if (selected) {
                    selectedUsers.add(userId)
                } else {
                    selectedUsers.remove(userId)
                }
            }
        }, selectMode)

        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
            binding.emptyView.isVisible = users.isEmpty()
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                // Показать ошибку
            }
        }
    }

    private fun returnSelectedUsers() {
        val result = Intent().apply {
            putExtra("selected_user_ids", selectedUsers.toLongArray())
        }
        requireActivity().setResult(Activity.RESULT_OK, result)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}