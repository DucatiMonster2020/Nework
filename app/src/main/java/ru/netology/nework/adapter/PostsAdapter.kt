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
import ru.netology.nework.databinding.ItemPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils

interface PostInteractionListener {
    fun onLikeClicked(post: Post)
    fun onShareClicked(post: Post)
    fun onPostClicked(post: Post)
    fun onAvatarClicked(post: Post)
    fun onLinkClicked(post: Post)
    fun onMenuClicked(post: Post, anchor: View)
    fun onAttachmentClicked(post: Post)
}
class PostsAdapter(
    private val listener: PostInteractionListener
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        private val listener: PostInteractionListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                textViewAuthor.text = post.author
                textViewPublished.text = post.formattedPublished
                textViewContent.text = post.content
                buttonLike.text = post.getLikesCount().toString()
                imageButtonLike.isChecked = post.likedByMe
                post.authorAvatar?.let { avatarUrl ->
                    Glide.with(imageViewAvatar)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .circleCrop()
                        .into(imageViewAvatar)
                } ?: run {
                    imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                }
                textViewAuthorJob.text = post.authorJob ?: "В поисках работы"
                textViewAuthorJob.isVisible = post.authorJob != null
                post.attachment?.let { attachment ->
                    when (attachment.type)
                     {
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
                    imageViewAttachment.isVisible = false
                }
                post.link?.let { link ->
                    textViewLink.text = link
                    textViewLink.isVisible = true
                } ?: run {
                    textViewLink.isVisible = false
                }
                imageButtonMenu.isVisible = post.ownedByMe
                buttonLike.setOnClickListener {
                    listener.onLikeClicked(post)
                }
                buttonShare.setOnClickListener {
                    listener.onShareClicked(post)
                }
                imageViewAvatar.setOnClickListener {
                    listener.onAvatarClicked(post)
                }
                imageButtonMenu.setOnClickListener {
                    listener.onMenuClicked(post, it)
                }
                textViewLink.setOnClickListener {
                    listener.onLinkClicked(post)
                }
                imageViewAttachment.setOnClickListener {
                    listener.onAttachmentClicked(post)
                }
                root.setOnClickListener {
                    listener.onPostClicked(post)
                }
                AndroidUtils.fixRecyclerViewItem(root)
            }
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