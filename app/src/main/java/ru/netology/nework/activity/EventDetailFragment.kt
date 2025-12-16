package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventDetailBinding
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by viewModels()
    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val args: EventDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
        loadEvent()
    }

    private fun setupClickListeners() {
        binding.buttonLike.setOnClickListener {
            viewModel.likeById(args.eventId)
        }

        binding.buttonParticipate.setOnClickListener {
            if (binding.buttonParticipate.isChecked) {
                viewModel.participateById(args.eventId)
            } else {
                viewModel.unparticipateById(args.eventId)
            }
        }

        binding.imageViewAvatar.setOnClickListener {
            findNavController().navigate(
                R.id.action_eventDetailFragment_to_userProfileFragment,
                Bundle().apply {
                    // Здесь нужно передать ID автора события
                }
            )
        }

        binding.imageButtonMenu.setOnClickListener {
            // Показываем меню для владельца события
        }

        binding.textViewLink.setOnClickListener {
            // Открываем ссылку в браузере
        }

        binding.imageViewAttachment.setOnClickListener {
            // Открываем вложение
        }

        binding.buttonShowMap.setOnClickListener {
            // Показываем карту с координатами
        }

        binding.buttonShowParticipants.setOnClickListener {
            showParticipantsDialog()
        }

        binding.buttonShowSpeakers.setOnClickListener {
            showSpeakersDialog()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventState.collectLatest { state ->
                // Обновляем UI на основе состояния
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }
    }

    private fun loadEvent() {
        viewModel.loadEventById(args.eventId)
    }

    private fun showParticipantsDialog() {
        // Показываем диалог со списком участников
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Участники события")
            .setMessage("Список участников будет загружен")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSpeakersDialog() {
        // Показываем диалог со списком спикеров
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Спикеры события")
            .setMessage("Список спикеров будет загружен")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}