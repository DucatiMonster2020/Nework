package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.ItemJobBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.util.AndroidUtils

interface JobInteractionListener {
    fun onJobClicked(job: Job)
    fun onEditClicked(job: Job)
    fun onDeleteClicked(job: Job)
    fun onLinkClicked(job: Job)
}
class JobsAdapter(
    private val listener: JobInteractionListener,
    private val editable: Boolean = false
) : ListAdapter<Job, JobsAdapter.JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {
        val binding = ItemJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding, listener, editable)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
    class JobViewHolder(
        private val binding: ItemJobBinding,
        private val listener: JobInteractionListener,
        private val editable: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.apply {
                textViewCompanyName.text = job.name
                textViewPosition.text = job.position
                textViewPeriod.text = job.formattedPeriod
                job.link?.let { link ->
                    textViewLink.text = link
                    textViewLink.isVisible = true
                    textViewLink.setOnClickListener {
                        listener.onLinkClicked(job)
                    }
                } ?: run {
                    textViewLink.isVisible = false
                }
                if (editable) {
                    buttonEdit.isVisible = true
                    buttonDelete.isVisible = true
                    buttonEdit.setOnClickListener {
                        listener.onEditClicked(job)
                    }
                    buttonDelete.setOnClickListener {
                        listener.onDeleteClicked(job)
                    }
                } else {
                    buttonEdit.isVisible = false
                }
                root.setOnClickListener {
                    listener.onJobClicked(job)
                }
                AndroidUtils.fixRecyclerViewItem(root)
                }
        }
    }
    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}