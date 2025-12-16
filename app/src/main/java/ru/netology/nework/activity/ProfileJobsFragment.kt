package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.adapter.JobsAdapter

@AndroidEntryPoint
class ProfileJobsFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private var _binding: FragmentProfileJobsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JobsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        loadJobs()
    }

    private fun setupRecyclerView() {
        adapter = JobsAdapter(object : JobsAdapter.JobInteractionListener {
            override fun onJobClick(jobId: Long) {
            }

            override fun onEditJob(jobId: Long) {
            }

            override fun onDeleteJob(jobId: Long) {
                viewModel.deleteJob(jobId)
            }
        })

        binding.jobsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.jobsRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            adapter.submitList(jobs)
            binding.emptyView.isVisible = jobs.isEmpty()
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                // Показать ошибку
                binding.errorView.isVisible = true
                binding.errorTextView.text = errorMessage
            } else {
                binding.errorView.isVisible = false
            }
        }
    }

    private fun loadJobs() {
        viewModel.loadJobs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}