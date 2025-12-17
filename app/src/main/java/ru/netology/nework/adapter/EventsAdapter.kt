package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventsAdapter(
    private val onLikeListener: (Event) -> Unit,
    private val onParticipateListener: (Event) -> Unit,
    private val onEditListener: (Event) -> Unit,
    private val onRemoveListener: (Event) -> Unit,
    private val onItemClickListener: (Event) -> Unit
) : PagingDataAdapter<Event, EventsAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }

    inner class ViewHolder(
        private val binding: CardEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                author.text = event.author
                published.text = formatDate(event.published)
                eventDate.text = formatDate(event.datetime)
                content.text = event.content
                eventType.text = if (event.type == EventType.OFFLINE) "Офлайн" else "Онлайн"

                // Аватар
                event.authorAvatar?.let { avatarUrl ->
                    Glide.with(avatar)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(avatar)
                } ?: run {
                    avatar.setImageResource(R.drawable.ic_avatar_placeholder)
                }

                // Лайки
                like.isChecked = event.likedByMe
                like.text = event.likeOwnerIds.size.toString()

                // Участие
                participate.isChecked = event.participatedByMe
                participantsCount.text = event.participantsIds.size.toString()

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
                } ?: run {
                    attachmentContainer.isVisible = false
                }

                // Ссылка
                link.isVisible = event.link != null
                event.link?.let { url ->
                    link.text = url
                }

                // Кнопка меню
                menu.isVisible = event.ownedByMe

                // Слушатели
                like.setOnClickListener {
                    onLikeListener(event)
                }

                participate.setOnClickListener {
                    onParticipateListener(event)
                }

                menu.setOnClickListener {
                    showPopupMenu(event)
                }

                root.setOnClickListener {
                    onItemClickListener(event)
                }
            }
        }

        private fun showPopupMenu(event: Event) {
            val popup = PopupMenu(binding.menu.context, binding.menu)
            popup.menuInflater.inflate(R.menu.event_options, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        onEditListener(event)
                        true
                    }
                    R.id.remove -> {
                        onRemoveListener(event)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun formatDate(instant: Instant): String {
            val formatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            return formatter.format(instant)
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