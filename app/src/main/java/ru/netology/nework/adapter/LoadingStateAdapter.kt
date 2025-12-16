package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.ItemLoadingStateBinding

class LoadingStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadingStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {
        val binding = ItemLoadingStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return LoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(
        private val binding: ItemLoadingStateBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.buttonRetry.setOnClickListener {
                retry()
            }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                buttonRetry.isVisible = loadState is LoadState.Error
                textViewError.isVisible = loadState is LoadState.Error
                if (loadState is LoadState.Error) {
                    textViewError.text = loadState.error.localizedMessage
                }
            }
        }
    }
}