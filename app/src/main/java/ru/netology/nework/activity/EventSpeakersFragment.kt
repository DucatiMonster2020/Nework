package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventSpeakersFragment : Fragment() {

    @Inject
    lateinit var viewModel: EventViewModel

    private var _binding: FragmentEventSpeakersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventSpeakersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupObservers()
    }

    private fun setupAdapter() {
        adapter = UsersAdapter(
            onUserClickListener = { user ->
                val action = EventDetailFragmentDirections
                    .actionEventDetailFragmentToUserProfileFragment(userId = user.id)
                findNavController().navigate(action)
            }
        )

        binding.speakersList.layoutManager = LinearLayoutManager(requireContext())
        binding.speakersList.adapter = adapter

        // TODO: Загрузить спикеров события
        lifecycleScope.launch {
            // viewModel.eventSpeakers.collectLatest { speakers ->
            //     adapter.submitData(speakers)
            // }
        }
    }

    private fun setupObservers() {
        viewModel.currentEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                binding.emptyState.isVisible = event.speakerIds.isEmpty()
                binding.speakersList.isVisible = event.speakerIds.isNotEmpty()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
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