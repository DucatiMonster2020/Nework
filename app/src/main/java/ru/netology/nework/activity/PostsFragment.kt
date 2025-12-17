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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by viewModels()

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostsAdapter

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

        setupAdapter()
        setupListeners()
        observeViewModel()
        observeAuthState()
    }

    private fun setupAdapter() {
        adapter = PostsAdapter(
            onLikeListener = { post ->
                if (post.likedByMe) {
                    viewModel.dislikeById(post.id)
                } else {
                    viewModel.likeById(post.id)
                }
            },
            onEditListener = { post ->
                if (appAuth.authState.value?.isAuthorized == true) {
                    findNavController().navigate(
                        R.id.action_postsFragment_to_newPostFragment,
                        Bundle().apply {
                            putLong("postId", post.id)
                        }
                    )
                }
            },
            onRemoveListener = { post ->
                viewModel.removeById(post.id)
            },
            onItemClickListener = { post ->
                findNavController().navigate(
                    R.id.action_postsFragment_to_postDetailFragment,
                    Bundle().apply {
                        putLong("postId", post.id)
                    }
                )
            }
        )

        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        binding.postsList.adapter = adapter

        // Обработка состояний загрузки
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->
                binding.progress.isVisible = loadState.refresh is LoadState.Loading
                binding.errorGroup.isVisible = loadState.refresh is LoadState.Error

                if (loadState.refresh is LoadState.Error) {
                    val error = (loadState.refresh as LoadState.Error).error
                    Snackbar.make(binding.root, error.message ?: "Ошибка", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Получение данных
        lifecycleScope.launch {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun setupListeners() {
        binding.fab.setOnClickListener {
            if (appAuth.authState.value?.isAuthorized == true) {
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.action_global_loginFragment)
            }
        }

        binding.retryButton.setOnClickListener {
            adapter.retry()
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, "Пост создан", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeAuthState() {
        appAuth.authState.observe(viewLifecycleOwner) { authState ->
            binding.fab.isVisible = authState.isAuthorized
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}