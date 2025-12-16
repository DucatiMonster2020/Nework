package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.dto.Post

@AndroidEntryPoint
class UserWallFragment : Fragment() {

    private var _binding: FragmentUserWallBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostsAdapter
    private var userId: Long = 0L

    private val viewModel: UserViewModel by viewModels()

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Long): UserWallFragment {
            return UserWallFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserWallBinding.inflate(inflater, container, false)
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
                // TODO: Реализовать лайк
            }

            override fun onClick(post: Post) {
                // TODO: Переход к детальному просмотру
            }

            override fun onEdit(post: Post) {
                // Только для своих постов
            }

            override fun onRemove(post: Post) {
                // Только для своих постов
            }

            override fun onAvatarClick(post: Post) {
                // Ничего не делаем, так как мы уже в профиле пользователя
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
        viewModel.loadUserWall(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}