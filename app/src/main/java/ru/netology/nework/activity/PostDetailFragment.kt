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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostDetailBinding
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by viewModels()
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val args: PostDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
        loadPost()
    }

    private fun setupClickListeners() {
        binding.imageButtonLike.setOnClickListener {
            viewModel.likeById(args.postId)
        }

        binding.imageButtonShare.setOnClickListener {
            sharePost()
        }

        binding.imageViewAvatar.setOnClickListener {
            // Переход к профилю автора
        }

        binding.imageButtonMenu.setOnClickListener {
            // Показываем меню для владельца поста
        }

        binding.textViewLink.setOnClickListener {
            // Открываем ссылку в браузере
        }

        binding.imageViewAttachment.setOnClickListener {
            // Открываем вложение
        }

        binding.buttonShowMap.setOnClickListener {
            // Показываем карту с координатами
        }

        binding.buttonShowMentionedUsers.setOnClickListener {
            // Показываем список упомянутых пользователей
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.feedState.collectLatest { state ->
                // Обновляем UI на основе состояния
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }
    }

    private fun loadPost() {
        viewModel.loadPostById(args.postId)
    }

    private fun sharePost() {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, "Посмотрите этот пост в Nework!")
            type = "text/plain"
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться постом"))
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}