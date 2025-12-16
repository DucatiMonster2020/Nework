package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.UserViewModel
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class UsersFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        setupClickListeners()
        setupObservers()
        loadPosts()
    }
    private fun setupRecycleView() {
        adapter = UsersAdapter(object : UsersAdapter.UserInteractionListener {
            override fun onUserClicked(user: User) {
                findNavController().navigate(
                    R.id.action_usersFragment_to_userProfileFragment,
                    Bundle().apply {
                        putLong("userId", user.id)
                    }
                )
            }

            override fun onUserSelected(user: User, selected: Boolean) {

            }
        }, isSelectionMode = false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL)
        )
    }
    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUsers()
        }
        binding.toolbar.setOnMenuClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login -> {
                    findNavController().navigate(R.id.action_usersFragment_to_loginFragment)
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.action_usersFragment_to_profileFragment)
                    true
                } else -> false
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchUsers(newText ?: "")
                return true
            }
        })
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest { users ->
                adapter.submitList(users)
                binding.swipeRefresh.isRefreshing = false
                binding.emptyView.isVisible = users.isEmpty()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            appAuth.authStateFlow.collect { authState ->
                updateMenu(authState.isAuthorized)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }
    }
    private fun loadPosts() {
        viewModel.loadUsers()
    }
    private fun updateMenu(isAuthorized: Boolean) {
        binding.toolbar.menu.clear()
        if (isAuthorized) {
            binding.toolbar.inflateMenu(R.menu.menu_authorized)
        } else {
            binding.toolbar.inflateMenu(R.menu.menu_unauthorized)
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