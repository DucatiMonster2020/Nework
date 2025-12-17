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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventDetailBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.viewmodel.EventViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by viewModels()
    private val args: EventDetailFragmentArgs by navArgs()

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        observeViewModel()
        loadEvent()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    if (appAuth.authState.value?.isAuthorized == true) {
                        findNavController().navigate(
                            R.id.action_eventDetailFragment_to_newEventFragment,
                            Bundle().apply {
                                putLong("eventId", args.eventId)
                            }
                        )
                    }
                    true
                }
                R.id.remove -> {
                    if (appAuth.authState.value?.isAuthorized == true) {
                        viewModel.removeById(args.eventId)
                        findNavController().navigateUp()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.likeButton.setOnClickListener {
            viewModel.likeById(args.eventId)
        }

        binding.participateButton.setOnClickListener {
            viewModel.participate(args.eventId)
        }

        binding.showMap.setOnClickListener {
            viewModel.event.value?.coords?.let { coords ->
                findNavController().navigate(
                    EventDetailFragmentDirections.actionEventDetailFragmentToMapFragment(
                        lat = coords.lat,
                        lng = coords.lng
                    )
                )
            }
        }

        binding.link.setOnClickListener {
            viewModel.event.value?.link?.let { url ->
                AndroidUtils.openUrl(requireContext(), url)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event?.let { bindEvent(it) }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadEvent() {
        lifecycleScope.launch {
            binding.progress.isVisible = true
            try {
                viewModel.loadEvent(args.eventId)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Ошибка загрузки события", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progress.isVisible = false
            }
        }
    }

    private fun bindEvent(event: Event) {
        binding.apply {
            authorName.text = event.author
            publishedDate.text = formatDate(event.published)
            eventDate.text = formatDate(event.datetime)
            content.text = event.content

            // Аватар
            event.authorAvatar?.let { avatarUrl ->
                Glide.with(authorAvatar)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(authorAvatar)
            }

            // Тип события
            eventType.text = if (event.type == EventType.OFFLINE) "Офлайн" else "Онлайн"

            // Лайки
            likeButton.isChecked = event.likedByMe
            likeButton.text = event.likeOwnerIds.size.toString()

            // Участие
            participateButton.isChecked = event.participatedByMe
            participantsCount.text = event.participantsIds.size.toString()

            // Последнее место работы
            authorJob.text = event.authorJob ?: "В поиске работы"
            authorJobContainer.isVisible = event.authorJob != null

            // Спикеры
            speakersContainer.isVisible = event.speakerIds.isNotEmpty()
            speakers.text = event.speakerIds.joinToString(", ") { id ->
                event.users[id]?.name ?: "Спикер $id"
            }
            participantsList.isVisible = event.participantsIds.isNotEmpty()
            participants.text = event.participantsIds.joinToString(", ") { id ->
                event.users[id]?.name ?: "Участник $id"
            }

            // Вложение
            event.attachment?.let { attachment ->
                attachmentContainer.isVisible = true
                when (attachment.type) {
                    AttachmentType.IMAGE -> {
                        Glide.with(attachmentImage)
                            .load(attachment.url)
                            .into(attachmentImage)
                    }
                    // Обработка AUDIO и VIDEO
                }
            }

            // Ссылка
            linkContainer.isVisible = event.link != null
            link.text = event.link

            // Координаты
            showMap.isVisible = event.coords != null

            // Меню редактирования
            toolbar.menu.findItem(R.id.edit).isVisible = event.ownedByMe
            toolbar.menu.findItem(R.id.remove).isVisible = event.ownedByMe
        }
    }

    private fun formatDate(instant: Instant): String {
        val formatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}