package ru.netology.nework.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val args: MapFragmentArgs by navArgs()

    private var placemark: PlacemarkMapObject? = null
    private var selectedPoint: Point? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            enableMyLocation()
        } else {
            showError("Для определения местоположения нужны разрешения")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем MapKit перед использованием карты
        MapKitFactory.initialize(requireContext())

        setupMap()
        setupClickListeners()
        setupObservers()
        checkLocationPermissions()
    }

    private fun setupMap() {
        binding.mapView.map.move(
            CameraPosition(
                Point(args.lat.toDouble(), args.lon.toDouble()),
                15.0f, 0.0f, 0.0f
            ),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        // Добавляем маркер
        placemark = binding.mapView.map.mapObjects.addPlacemark(
            Point(args.lat.toDouble(), args.lon.toDouble())
        )

        // Добавляем обработчик кликов по карте
        binding.mapView.map.addTapListener { map, point ->
            selectedPoint = point
            updateMarker(point)
            true
        }
    }

    private fun setupClickListeners() {
        binding.fabMyLocation.setOnClickListener {
            enableMyLocation()
        }

        binding.fabSelect.setOnClickListener {
            selectedPoint?.let { point ->
                // Возвращаем выбранные координаты
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "selected_coordinates",
                    ru.netology.nework.dto.Coordinates(point.latitude, point.longitude)
                )
                findNavController().popBackStack()
            } ?: showError("Выберите место на карте")
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error.getUserMessage())
            }
        }
    }

    private fun checkLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    private fun enableMyLocation() {
        try {
            binding.mapView.map.isZoomGesturesEnabled = true
            binding.mapView.map.isScrollGesturesEnabled = true
            binding.mapView.map.isRotateGesturesEnabled = true

            // Включаем отображение местоположения пользователя
            val mapKit = MapKitFactory.getInstance()
            val userLocationLayer = mapKit.createUserLocationLayer(binding.mapView.mapWindow)
            userLocationLayer.isVisible = true
            userLocationLayer.isHeadingEnabled = true

        } catch (e: SecurityException) {
            showError("Нет разрешения на доступ к местоположению")
        }
    }

    private fun updateMarker(point: Point) {
        placemark?.geometry = point

        // Обновляем текст с координатами
        binding.textViewCoordinates.text =
            "Широта: ${String.format("%.6f", point.latitude)}\n" +
                    "Долгота: ${String.format("%.6f", point.longitude)}"
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}