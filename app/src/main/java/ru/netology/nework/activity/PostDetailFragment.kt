package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentPostDetailBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.ValidationUtils
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class PostDetailFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: PostViewModel by viewModels()
    private val args: PostDetailFragmentArgs by navArgs()

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private var mapInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupMap()
        setupObservers()
        setupClickListeners()

        viewModel.loadPost(args.postId)
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
        viewModel.currentPost.observe(viewLifecycleOwner) { post ->
            post?.let {
                bindPost(it)
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
            viewModel.currentPost.value?.let { post ->
                viewModel.likeById(post.id)
            }
        }

        binding.shareButton.setOnClickListener {
            viewModel.currentPost.value?.let { post ->
                AndroidUtils.shareContent(
                    requireContext(),
                    "${post.content}\n\nПоделиться из NeWork",
                    "Поделиться постом"
                )
            }
        }

        binding.linkTextView.setOnClickListener {
            viewModel.currentPost.value?.link?.let { url ->
                AndroidUtils.openUrl(requireContext(), url)
            }
        }
    }

    private fun bindPost(post: ru.netology.nework.dto.Post) {
        // Автор
        binding.authorName.text = post.author
        Glide.with(binding.authorAvatar)
            .load(post.authorAvatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_placeholder)
            .into(binding.authorAvatar)

        // Дата
        binding.published.text = ValidationUtils.formatDateTime(post.published)

        // Текст
        binding.content.text = post.content

        // Лайки
        binding.likeButton.isChecked = post.likedByMe
        binding.likeCount.text = AndroidUtils.formatCount(post.likes)

        // Вложение
        val attachment = post.attachment
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
        binding.linkGroup.isVisible = !post.link.isNullOrBlank()
        post.link?.let { link ->
            binding.linkTextView.text = AndroidUtils.extractDomain(link)
        }

        // Меню
        binding.toolbar.menu.findItem(R.id.edit).isVisible = post.ownedByMe
        binding.toolbar.menu.findItem(R.id.delete).isVisible = post.ownedByMe

        // Координаты и карта
        val hasCoordinates = post.coords != null
        binding.mapGroup.isVisible = hasCoordinates

        if (hasCoordinates && mapInitialized) {
            post.coords?.let { coords ->
                val lat = coords.lat.toDoubleOrNull()
                val long = coords.long.toDoubleOrNull()
                if (lat != null && long != null) {
                    binding.coordinatesText.text = "Широта: ${String.format("%.6f", lat)}\n" + "Долгота: ${String.format("%.6f", long)}"
                }

            }
        }

        // Упомянутые пользователи
        binding.mentionedUsersGroup.isVisible = post.mentionIds.isNotEmpty()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapInitialized = true

        // Настройка карты
        map.uiSettings.isZoomControlsEnabled = true

        // Если пост уже загружен, отображаем координаты
        viewModel.currentPost.value?.coords?.let { coords ->
            val location = LatLng(coords.lat, coords.long)
            map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Местоположение поста")
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}