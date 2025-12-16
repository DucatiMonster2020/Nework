package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils

interface PostInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onVideoPlay(post: Post, videoUrl: String) {}
    fun onAudioPlay(post: Post, audioUrl: String) {}
    fun onClick(post: Post) {}
    fun onAvatarClick(post: Post) {}
    fun onLinkClick(url: String) {}
}

class PostsAdapter(
    private val interactionListener: PostInteractionListener
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardPostBinding.inflate(inflater, parent, false)
        return PostViewHolder(binding, interactionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        if (post != null) {
            holder.bind(post)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val interactionListener: PostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    private var currentPost: Post? = null

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.likeButton.setOnClickListener {
            currentPost?.let { post ->
                interactionListener.onLike(post)
            }
        }

        binding.menuButton.setOnClickListener {
            currentPost?.let { post ->
                showPopupMenu(post)
            }
        }

        binding.shareButton.setOnClickListener {
            currentPost?.let { post ->
                interactionListener.onShare(post)
            }
        }

        binding.root.setOnClickListener {
            currentPost?.let { post ->
                interactionListener.onClick(post)
            }
        }

        binding.authorAvatar.setOnClickListener {
            currentPost?.let { post ->
                interactionListener.onAvatarClick(post)
            }
        }

        binding.attachmentImageView.setOnClickListener {
            currentPost?.attachment?.let { attachment ->
                when (attachment.type) {
                    Attachment.Type.VIDEO -> {
                        interactionListener.onVideoPlay(currentPost!!, attachment.url)
                    }
                    Attachment.Type.IMAGE -> {

                    }
                    else -> {}
                }
            }
        }

        binding.linkTextView.setOnClickListener {
            currentPost?.link?.let { url ->
                interactionListener.onLinkClick(url)
            }
        }
    }

    fun bind(post: Post) {
        currentPost = post

        binding.authorName.text = post.author
        Glide.with(binding.authorAvatar)
            .load(post.authorAvatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.authorAvatar)
        binding.published.text = ValidationUtils.formatPublishedDateTime(post.published)
        binding.content.text = post.content
        binding.likeButton.isChecked = post.likedByMe
        binding.likeCount.text = AndroidUtils.formatCount(post.likes)

        val attachment = post.attachment
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
                }
                Attachment.Type.VIDEO -> {
                    binding.attachmentImageView.isVisible = true
                    binding.audioView.isVisible = false
                    binding.videoView.isVisible = true
                    Glide.with(binding.attachmentImageView)
                        .load(attachment.previewUrl)
                        .into(binding.attachmentImageView)
                }
            }
        }
        binding.linkGroup.isVisible = !post.link.isNullOrBlank()
        post.link?.let { link ->
            binding.linkTextView.text = AndroidUtils.extractDomain(link)
        }
        binding.menuButton.isVisible = post.ownedByMe
    }
    private fun showPopupMenu(post: Post) {
        AndroidUtils.showPopupMenu(
            binding.menuButton,
            menuRes = R.menu.post_menu,
            onEditClick = { interactionListener.onEdit(post) },
            onDeleteClick = { interactionListener.onRemove(post) }
        )
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