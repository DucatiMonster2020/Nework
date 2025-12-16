package ru.netology.nework.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.PostRequest
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels()
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private var selectedMentionIds = emptyList<Long>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
    }
    private fun setupClickListeners() {
        binding.apply {
            buttonMention.setOnClickListener {
                findNavController().navigate(R.id.action_newPostFragment_to_usersFragment)
            }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.save -> {
                        savePost()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    private fun savePost() {
        val content = binding.editTextContent.tex.toString().trim()
        if (content.isBlank()) {
            AndroidUtils.showToast(requireContext(), "Введите текст поста")
            return
        }
        val post = PostRequest(
            content = content,
            mentionIds = selectedMentionIds
        )
        viewModel.save(post)
        findNavController().popBackStack()
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                AndroidUtils.showToast(requireContext(), error.message ?: "Ошиька")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}