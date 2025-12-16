package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.PostsAdapter

@AndroidEntryPoint
class ProfileWallFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private var _binding: FragmentProfileWallBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileWallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        loadWall()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter(object : PostsAdapter.PostInteractionListener {
            override fun onLike(post: ru.netology.nework.dto.Post) {
                viewModel.likePost(post.id)
            }

            override fun onClick(post: ru.netology.nework.dto.Post) {
            }

            override fun onEdit(post: ru.netology.nework.dto.Post) {
            }

            override fun onRemove(post: ru.netology.nework.dto.Post) {
                viewModel.deletePost(post.id)
            }

            override fun onAvatarClick(post: ru.netology.nework.dto.Post) {
            }
        })

        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.postsRecyclerView.adapter = adapter

        adapter.addLoadStateListener { loadState ->
            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
            binding.emptyView.isVisible = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
            binding.errorView.isVisible = loadState.refresh is LoadState.Error
        }

        binding.retryButton.setOnClickListener {
            adapter.retry()
        }
    }

    private fun setupObservers() {
        viewModel.wallPosts.observe(viewLifecycleOwner) { pagingData ->
            lifecycleScope.launch {
                adapter.submitData(pagingData)
            }
        }
    }

    private fun loadWall() {
        viewModel.loadWall()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}