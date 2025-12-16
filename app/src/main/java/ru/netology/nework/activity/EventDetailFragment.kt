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
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventDetailBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: EventViewModel by viewModels()
    private val args: EventDetailFragmentArgs by navArgs()

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private var mapInitialized = false

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

        setupToolbar()
        setupMap()
        setupObservers()
        setupClickListeners()

        viewModel.loadEvent(args.eventId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        // TODO: Реализовать редактирование
                        true
                    }
                    R.id.delete -> {
                        // TODO: Реализовать удаление
                        true
                    }
                    R.id.participate -> {
                        viewModel.currentEvent.value?.let { event ->
                            viewModel.participate(event.id)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupObservers() {
        viewModel.currentEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                bindEvent(it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                AndroidUtils.showToast(requireContext(), errorMessage)
            }
        }
    }

    private fun setupClickListeners() {
        binding.likeButton.setOnClickListener {
            viewModel.currentEvent.value?.let { event ->
                viewModel.likeById(event.id)
            }
        }

        binding.shareButton.setOnClickListener {
            viewModel.currentEvent.value?.let { event ->
                AndroidUtils.shareContent(
                    requireContext(),
                    "${event.content}\n\nСобытие в NeWork\nДата: ${ValidationUtils.formatDateTime(event.datetime)}",
                    "Поделиться событием")
            }
        }

        binding.linkTextView.setOnClickListener {
            viewModel.currentEvent.value?.link?.let { url ->
                AndroidUtils.openUrl(requireContext(), url)
            }
        }
    }

    private fun bindEvent(event: ru.netology.nework.dto.Event) {
        // Автор
        binding.authorName.text = event.author
        Glide.with(binding.authorAvatar)
            .load(event.authorAvatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.authorAvatar)

        // Даты
        binding.published.text = ValidationUtils.formatDateTime(event.published)
        binding.eventDateTime.text = ValidationUtils.formatDateTime(event.datetime)

        // Тип события
        binding.eventType.text = when (event.type) {
            ru.netology.nework.dto.EventType.ONLINE -> getString(R.string.online)
            ru.netology.nework.dto.EventType.OFFLINE -> getString(R.string.offline)
        }

        // Текст
        binding.content.text = event.content

        // Лайки
        binding.likeButton.isChecked = event.likedByMe
        binding.likeCount.text = AndroidUtils.formatCount(event.likes)

        // Участие
        binding.participantsCount.text = event.participantsIds.size.toString()
        binding.toolbar.menu.findItem(R.id.participate).title =
            if (event.participatedByMe) getString(R.string.leave) else getString(R.string.participate)

        // Вложение
        val attachment = event.attachment
        binding.attachmentGroup.isVisible = attachment != null
        if (attachment != null) {
            when (attachment.type) {
                ru.netology.nework.dto.Attachment.Type.IMAGE -> {
                    binding.attachmentImageView.isVisible = true
                    binding.audioView.isVisible = false
                    binding.videoView.isVisible = false
                    Glide.with(binding.attachmentImageView)
                        .load(attachment.url)
                        .into(binding.attachmentImageView)
                }
                ru.netology.nework.dto.Attachment.Type.AUDIO -> {
                    binding.attachmentImageView.isVisible = false
                    binding.audioView.isVisible = true
                    binding.videoView.isVisible = false
                }
                ru.netology.nework.dto.Attachment.Type.VIDEO -> {
                    binding.attachmentImageView.isVisible = true
                    binding.audioView.isVisible = false
                    binding.videoView.isVisible = true
                    Glide.with(binding.attachmentImageView)
                        .load(attachment.previewUrl)
                        .into(binding.attachmentImageView)
                }
            }
        }

        // Ссылка
        binding.linkGroup.isVisible = !event.link.isNullOrBlank()
        event.link?.let { link ->
            binding.linkTextView.text = AndroidUtils.extractDomain(link)
        }

        // Меню
        binding.toolbar.menu.findItem(R.id.edit).isVisible = event.ownedByMe
        binding.toolbar.menu.findItem(R.id.delete).isVisible = event.ownedByMe

        // Координаты и карта
        val hasCoordinates = event.coords != null
        binding.mapGroup.isVisible = hasCoordinates

        if (hasCoordinates && mapInitialized) {
            event.coords?.let { coords ->
                val location = LatLng(coords.lat, coords.long)
                map.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Местоположение события")
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapInitialized = true

        // Настройка карты
        map.uiSettings.isZoomControlsEnabled = true

        // Если событие уже загружено, отображаем координаты
        viewModel.currentEvent.value?.coords?.let { coords ->
            val location = LatLng(coords.lat, coords.long)
            map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Местоположение события")
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}