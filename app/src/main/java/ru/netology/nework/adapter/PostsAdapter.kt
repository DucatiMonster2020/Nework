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
import ru.netology.nework.dto.Post
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PostsAdapter(
    private val onLikeListener: (Post) -> Unit,
    private val onEditListener: (Post) -> Unit,
    private val onRemoveListener: (Post) -> Unit,
    private val onItemClickListener: (Post) -> Unit
) : PagingDataAdapter<Post, PostsAdapter.ViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

    inner class ViewHolder(
        private val binding: CardPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                author.text = post.author
                published.text = formatDate(post.published)
                content.text = post.content

                // Аватар
                post.authorAvatar?.let { avatarUrl ->
                    Glide.with(avatar)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(avatar)
                } ?: run {
                    avatar.setImageResource(R.drawable.ic_avatar_placeholder)
                }
                // Лайки
                like.isChecked = post.likedByMe
                like.text = post.likeOwnerIds.size.toString()

                // Вложение
                post.attachment?.let { attachment ->
                    attachmentContainer.isVisible = true
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            Glide.with(attachmentImage)
                                .load(attachment.url)
                                .into(attachmentImage)
                            attachmentImage.isVisible = true
                            attachmentAudio.isVisible = false
                            attachmentVideo.isVisible = false
                        }
                        // Добавить обработку AUDIO и VIDEO
                    }
                } ?: run {
                    attachmentContainer.isVisible = false
                }

                // Ссылка
                link.isVisible = post.link != null
                post.link?.let { url ->
                    link.text = url
                }

                // Кнопка меню (только для своих постов)
                menu.isVisible = post.ownedByMe

                // Слушатели
                like.setOnClickListener {
                    onLikeListener(post)
                }

                menu.setOnClickListener {
                    showPopupMenu(post)
                }

                root.setOnClickListener {
                    onItemClickListener(post)
                }

                // Упомянутые пользователи
                mentionContainer.isVisible = post.mentionIds.isNotEmpty()
                mentions.text = post.mentionIds.joinToString(", ") { id ->
                    post.users[id]?.name ?: "Пользователь $id"
                }
            }
        }
        private fun showPopupMenu(post: Post) {
            val popup = PopupMenu(binding.menu.context, binding.menu)
            popup.menuInflater.inflate(R.menu.post_options, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        onEditListener(post)
                        true
                    }
                    R.id.remove -> {
                        onRemoveListener(post)
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

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}