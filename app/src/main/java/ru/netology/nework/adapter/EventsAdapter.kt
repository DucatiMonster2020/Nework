package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.ItemEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.util.AndroidUtils

interface EventInteractionListener {
    fun onLikeClicked(event: Event)
    fun onParticipateClicked(event: Event)
    fun onEventClicked(event: Event)
    fun onAvatarClicked(event: Event)
    fun onLinkClicked(event: Event)
    fun onMenuClicked(event: Event, anchor: View)
    fun onAttachmentClicked(event: Event)
    fun onSpeakersClicked(event: Event)
    fun onParticipantsClicked(event: Event)
}
class EventsAdapter(
    private val listener: EventInteractionListener
) : ListAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.bind(event)
        }
    }
    class EventViewHolder(
        private val binding: ItemEventBinding,
        private val listener: EventInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                textViewAuthor.text = event.author
                textViewPublished.text = event.formattedPublished
                textViewDateTime.text = event.formattedDatetime
                textViewContent.text = event.content
                buttonLike.text = event.getLikesCount().toString()
                imageButtonLike.isChecked = event.likedByMe
                buttonParticipants.text = event.getParticipantsCount().toString()
                buttonParticipate.isChecked = event.participatedByMe
                textViewEventType.text = when (event.type) {
                    EventType.ONLINE -> "Онлайн"
                    EventType.OFFLINE -> "Офлайн"
                }
                buttonSpeakers.text = event.getSpeakersCount().toString()
                event.authorAvatar?.let { avatarUrl ->
                    Glide.with(imageViewAvatar)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .circleCrop()
                        .into(imageViewAvatar)
                } ?: run {
                    imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                }
                textViewAuthorJob.text = event.authorJob ?: "В поиске работы"
                textViewAuthorJob.isVisible = event.authorJob != null
                event.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            imageViewAttachment.isVisible = true
                            Glide.with(imageViewAttachment)
                                .load(attachment.url)
                                .into(imageViewAttachment)
                        }
                        AttachmentType.AUDIO -> {}
                        AttachmentType.VIDEO -> {}
                    }
                } ?: run {
                    imageViewAttachment.setVisible(false)
                }
                event.link?.let { link ->
                    textViewLink.text = linktextViewLink.isVisible = true
                } ?: run {
                    textViewLink.isVisible = false
                }
                imageButtinMenu.setVisible(event.ownedByMe)
                buttonLike.setOnCLickListener {
                    listener.onLikeClicked(event)
                }
                buttonParticipate.setOnClickListener {
                    listener.onParticipateClicked(event)
                }
                imageViewAvatar.setOnCLickListener {
                    listener.onAvatarClicked(event)
                }
                imageButtonMenu.setOnCLickListener {
                    listener.onMenuClicked(event, it)
                }
                textViewLink.setOnClickListener {
                    listener.onLinkClicked(event)
                }
                imageViewAttachment.setOnClickListener {
                    listener.onAttachmentClicked(event)
                }
                textViewSpeakersCount.setOnCLickListener {
                    listener.onSpeakersClicked(event)
                }
                textViewParticipantsCount.setOnClickListener {
                    listener.onParticipantsClicked(event)
                }
                root.setOnClickListener {
                    listener.onEventClicked(event)
                }
                AndroidUtils.fixRecyclerViewItem(root)
            }
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
}