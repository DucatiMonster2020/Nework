package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.ItemUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.util.AndroidUtils

interface UserInteractionListener {
    fun onUserClicked(user: User)
    fun onUserSelected(user: User, selected: Boolean)
}
class UsersAdapter(
    private val listener: UserInteractionListener,
    private val isSelectionMode: Boolean = false
) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback()) {
    private val selectedUsers = mutableSetOf<Long>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, listener, isSelectionMode)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        val isSelected = selectedUsers.contains(user.id)
        holder.bind(user, isSelected)
    }
    fun setSelectedUsers(ids: List<Long>) {
        selectedUsers.clear()
        selectedUsers.addAll(ids)
        notifyDataSetChanged()
    }
    fun getSelectedUserIds(): List<Long> = selectedUsers.toList()
    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val listener: UserInteractionListener,
        private val isSelectionMode: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, isSelected: Boolean) {
            binding.apply {
                textViewLogin.text = user.login
                textViewName.text = user.name
                user.avatar?.let { avatarUrl ->
                    Glide.with(imageViewAvatar).load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .circleCrop()
                        .into(imageViewAvatar)
                } ?: run {
                    imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                }
                if (isSelectionMode) {
                    root.isSelected = isSelected
                    root.setOnClickListener {
                        val newSelected = !isSelected
                        root.isSelected = newSelected
                        listener.onUserSelected(user, newSelected)
                    }
                } else {
                    root.setOnClickListener {
                        listener.onUserClicked(user)
                    }
                }
                AndroidUtils.fixRecyclerViewItem(binding.root)
            }
        }
    }
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}