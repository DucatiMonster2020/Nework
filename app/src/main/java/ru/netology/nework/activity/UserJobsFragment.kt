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
class UserJobsFragment : Fragment() {

    private var _binding: FragmentUserJobsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JobsAdapter
    private var userId: Long = 0L

    private val viewModel: UserViewModel by viewModels()

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Long): UserJobsFragment {
            return UserJobsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserJobsBinding.inflate(inflater, container, false)
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
                // TODO: Детальный просмотр работы
            }

            override fun onEditJob(jobId: Long) {
                // Только для своих работ
            }

            override fun onDeleteJob(jobId: Long) {
                // Только для своих работ
            }
        }, false) // false - только просмотр

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
                binding.errorView.isVisible = true
                binding.errorTextView.text = errorMessage
            } else {
                binding.errorView.isVisible = false
            }
        }
    }

    private fun loadJobs() {
        viewModel.loadUserJobs(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
