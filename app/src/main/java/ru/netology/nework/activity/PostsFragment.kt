package ru.netology.nework.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
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
        setupRecycleView()
        setupClickListeners()
        setupObservers()
        loadPosts()
    }
    private fun setupRecycleView() {
        adapter = PostsAdapter.PostInteractionListener {
            override fun onLikeClicked(post: Post) {
                if (post.likedByMe) {
                    viewModel
                        .unlikeById(post.id)
                } else {
                    viewModel.likeById(post.id)
                }
            }

            override fun onShareClicked(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_post)))
            }

            override fun onPostClicked(post: Post) {
                findNavController().navigate(
                    R.id.action_postsFragment_to_postDetailFragment,
                    Bundle().apply {
                        putLong("postId", post.id)
                    }
                )
            }

            override fun onAvatarClicked(post: Post) {
                findNavController().navigate(
                    R.id.action_postsFragment_to_userProfileFragment,
                    Bundle().apply {
                        putLong("userId", post.authorId)
                    }
                )
            }

            override fun onLinkClicked(post: Post) {
                post.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }
            }

            override fun onMenuClicked(post: Post, anchor: View) {
                if (post.ownedByMe) {
                    showPostMenu(post, anchor)
                }
            }

            override fun onAttachmentClicked(post: Post) {
                post.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            val bundle = Bundle().apply {
                                putString("imageUrl", attachment.url)
                            }
                            findNavController().navigate(
                                R.id.action_postsFragment_to_imageViewerFragment,
                                bundle
                            )
                        }

                        AttachmentType.AUDIO -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.url))
                            startActivity(intent)
                        }

                        AttachmentType.VIDEO -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.url))
                            startActivity(intent)
                        }
                    }
                }
            }
        }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL)
        )
        adapter.addLoadStateListener { loadState ->
            binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
        }
    }
    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            if (appAuth.authStateFlow.value.isAuthorized) {
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.action_postsFragment_to_loginFragment)
            }
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.toolbar.setOnMenuClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login -> {
                    findNavController().navigate(R.id.action_postsFragment_to_loginFragment)
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.action_postsFragment_to_profileFragment)
                    true
                } else -> false
            }
        }
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.data.collectLatest { pagingData ->
                adapter.submitData(pagingData)
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
        viewModel.loadPosts()
    }
    private fun showPostMenu(post: Post, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.post_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        findNavController().navigate(
                            R.id.action_postsFragment_to_newPostFragment, Bundle().apply {
                                putLong("postId", post.id)
                            }
                        )
                        true
                    }
                    R.id.delete -> {
                        viewModel.removeById(post.id)
                        true
                    } else -> false
                }
            }
            show()
        }
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