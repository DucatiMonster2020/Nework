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
import ru.netology.nework.adapter.EventsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by viewModels()

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
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
        adapter = EventsAdapter(
            onLikeListener = { event ->
                if (event.likedByMe) {
                    viewModel.dislikeById(event.id)
                } else {
                    viewModel.likeById(event.id)
                }
            },
            onParticipateListener = { event ->
                if (event.participatedByMe) {
                    viewModel.cancelParticipation(event.id)
                } else {
                    viewModel.participate(event.id)
                }
            },
            onEditListener = { event ->
                if (appAuth.authState.value?.isAuthorized == true) {
                    findNavController().navigate(
                        R.id.action_eventsFragment_to_newEventFragment, Bundle().apply {
                            putLong("eventId", event.id)
                        }
                    )
                }
            },
            onRemoveListener = { event ->
                viewModel.removeById(event.id)
            },
            onItemClickListener = { event ->
                findNavController().navigate(
                    R.id.action_eventsFragment_to_eventDetailFragment,
                    Bundle().apply {
                        putLong("eventId", event.id)
                    }
                )
            }
        )

        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsList.adapter = adapter

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

        lifecycleScope.launch {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun setupListeners() {
        binding.fab.setOnClickListener {
            if (appAuth.authState.value?.isAuthorized == true) {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
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

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, "Событие создано", Snackbar.LENGTH_SHORT).show()
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