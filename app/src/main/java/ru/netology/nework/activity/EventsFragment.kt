package ru.netology.nework.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import ru.netology.nework.adapter.EventsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: EventViewModel by viewModels()
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding
    private lateinit var adapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventsBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        setupClickListeners()
        setupObservers()
        loadEvents()
    }
    private fun setupRecycleView() {
        adapter = EventsAdapter(object : EventsAdapter.EventInteractionListener {
            override fun onLikeClicked(event: Event) {
                if (event.likedByMe) {
                    viewModel.unlikeById(event.id)
                } else {
                    viewModel.likeById(event.id)
                }
            }

            override fun onParticipateClicked(event: Event) {
                if (event.participatedByMe) {
                    viewModel.participateById(event.id)
                } else {
                    viewModel.participateById(event.id)
                }
            }

            override fun onEventClicked(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_eventDetailFragment, Bundle().apply {
                        putLong("eventId", event.id)
                    }
                )
            }

            override fun onAvatarClicked(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_userProfileFragment,
                    Bundle().apply {
                        putLong("userId", event.authorId)
                    }
                )
            }

            override fun onLinkClicked(event: Event) {
                event.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }
            }

            override fun onMenuClicked(event: Event, anchor: View) {
                if (event.ownedByMe) {
                    showEventMenu(event, anchor)
                }
            }

            override fun onAttachmentClicked(event: Event) {
                event.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            val bundle = Bundle().apply {
                                putString("imageUrl", attachment.url)
                            }
                            findNavController().navigate(
                                R.id.action_events_Fragment_to_imageViewerFragment,
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

            override fun onSpeakersClicked(event: Event) {
                showSpeakersDialog(event)
            }

            override fun onParticipantsClicked(event: Event) {
                showParticipantsDialog(event)
            }
        }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.addLoadStateListener { loadState ->
            binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
        }
    }
    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            if (appAuth.authStateFlow.value.isAuthorized) {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            } else {
                findNavController().navigate(R.id.action_evventsFragment_to_loginFragment)
            }
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.toolbar.setOnMenuClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_loginFragment)
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_profileFragment)
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
    private fun loadEvents() {
        viewModel.loadEvents()
    }
    private fun showEventMenu(event: Event, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.event_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        findNavController().navigate(
                            R.id.action_eventsFragment_to_newEventFragment,
                            Bundle().apply {
                                putLong("eventId", event.id)
                            }
                        )
                        true
                    }

                    R.id.delete -> {
                        viewModel.removeBiId(event.id)
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }
    private fun showParticipantsDialog(event: Event) {
        val participants = event.participants.values.toList()
        if (participants.isNotEmpty()) {
            val names = participants.joinToString("\n") { it.name }
            AlertDialog.Builder(requireContext())
                .setTitle("Участники")
                .setMessage(names)
                .setPositiveButton("OK", null)
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
