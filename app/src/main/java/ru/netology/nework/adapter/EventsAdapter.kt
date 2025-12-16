package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils

interface EventInteractionListener {
    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onParticipate(event: Event) {}
    fun onShare(event: Event) {}
    fun onVideoPlay(event: Event, videoUrl: String) {}
    fun onAudioPlay(event: Event, audioUrl: String) {}
    fun onClick(event: Event) {}
    fun onAvatarClick(event: Event) {}
    fun onLinkClick(url: String) {}
}

class EventsAdapter(
    private val interactionListener: EventInteractionListener
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding, interactionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.bind(event)
        }
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val interactionListener: EventInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    private var currentEvent: Event? = null

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.likeButton.setOnClickListener {
            currentEvent?.let { event ->
                interactionListener.onLike(event)
            }
        }

        binding.menuButton.setOnClickListener {
            currentEvent?.let { event ->
                showPopupMenu(event)
            }
        }

        binding.shareButton.setOnClickListener {
            currentEvent?.let { event ->
                interactionListener.onShare(event)
            }
        }

        binding.participateButton.setOnClickListener {
            currentEvent?.let { event ->
                interactionListener.onParticipate(event)
            }
        }

        binding.root.setOnClickListener {
            currentEvent?.let { event ->
                interactionListener.onClick(event)
            }
        }

        binding.authorAvatar.setOnClickListener {
            currentEvent?.let { event ->
                interactionListener.onAvatarClick(event)
            }
        }

        binding.attachmentImageView.setOnClickListener {
            currentEvent?.attachment?.let { attachment ->
                when (attachment.type) {
                    Attachment.Type.VIDEO -> {
                        interactionListener.onVideoPlay(currentEvent!!, attachment.url)
                    }
                    Attachment.Type.IMAGE -> {
                        // Показать полноэкранное изображение
                    }
                    else -> {}
                }
            }
        }

        binding.linkTextView.setOnClickListener {
            currentEvent?.link?.let { url ->
                interactionListener.onLinkClick(url)
            }
        }
    }

    fun bind(event: Event) {
        currentEvent = event

        // Автор
        binding.authorName.text = event.author
        Glide.with(binding.authorAvatar)
            .load(event.authorAvatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.authorAvatar)

        // Даты по ТЗ: dd.MM.yyyy HH:mm
        binding.published.text = ValidationUtils.formatPublishedDateTime(event.published)
        binding.eventDateTime.text = ValidationUtils.formatEventDateTime(event)

        // Тип события
        binding.eventType.text = when (event.type) {
            EventType.ONLINE -> binding.root.context.getString(R.string.online)
            EventType.OFFLINE -> binding.root.context.getString(R.string.offline)
        }

        // Текст события
        binding.content.text = event.content

        // Лайки
        binding.likeButton.isChecked = event.likedByMe
        binding.likeCount.text = AndroidUtils.formatCount(event.likes)

        // Участие
        binding.participateButton.isChecked = event.participatedByMe
        binding.participantsCount.text = event.participantsIds.size.toString()

        // Вложение
        val attachment = event.attachment
        binding.attachmentGroup.isVisible = attachment != null
        if (attachment != null) {
            when (attachment.type) {
                Attachment.Type.IMAGE -> {
                    binding.attachmentImageView.isVisible = true
                    binding.audioView.isVisible = false
                    binding.videoView.isVisible = false
                    Glide.with(binding.attachmentImageView)
                        .load(attachment.url)
                        .into(binding.attachmentImageView)
                }
                Attachment.Type.AUDIO -> {
                    binding.attachmentImageView.isVisible = false
                    binding.audioView.isVisible = true
                    binding.videoView.isVisible = false
                    // Настройка аудиоплеера
                }
                Attachment.Type.VIDEO -> {
                    binding.attachmentImageView.isVisible = true
                    binding.audioView.isVisible = false
                    binding.videoView.isVisible = true
                    Glide.with(binding.attachmentImageView)
                        .load(attachment.previewUrl ?: attachment.url)
                        .into(binding.attachmentImageView)
                }
            }
        }

        // Ссылка
        binding.linkGroup.isVisible = !event.link.isNullOrBlank()
        event.link?.let { link ->
            binding.linkTextView.text = AndroidUtils.extractDomain(link)
        }

        // Меню (только для своих событий)
        binding.menuButton.isVisible = event.ownedByMe

        // Спикеры (если есть)
        val hasSpeakers = event.speakerIds.isNotEmpty()
        binding.speakersGroup.isVisible = hasSpeakers
        if (hasSpeakers) {
            val speakerNames = event.speakers.take(3).joinToString(", ") { it.name }
            binding.speakersText.text = speakerNames
            if (event.speakerIds.size > 3) {
                binding.speakersMore.text = binding.root.context.getString(
                    R.string.and_more,
                    event.speakerIds.size - 3
                )
            } else {
                binding.speakersMore.text = ""
            }
        }
    }

    private fun showPopupMenu(event: Event) {
        AndroidUtils.showPopupMenu(
            binding.menuButton,
            menuRes = R.menu.event_menu,
            onEditClick = { interactionListener.onEdit(event) },
            onDeleteClick = { interactionListener.onRemove(event) }
        )
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}