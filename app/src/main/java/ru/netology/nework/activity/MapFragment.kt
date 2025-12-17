package ru.netology.nework.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val args: MapFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null

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

        setupToolbar()
        setupMap()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.title = "Выбор локации"

        binding.saveLocation.setOnClickListener {
            googleMap?.let { map ->
                val target = map.cameraPosition.target
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "selected_location",
                    LatLng(target.latitude, target.longitude)
                )
                findNavController().navigateUp()
            }
        }
    }
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.map, newMapFragment)
                .commit()
            newMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Проверка разрешений
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        }

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        // Установка начальной позиции
        val initialLat = args.lat
        val initialLng = args.lng

        if (initialLat != 0.0 && initialLng != 0.0) {
            val initialPosition = LatLng(initialLat, initialLng)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15f))

            // Добавление маркера
            map.addMarker(
                MarkerOptions()
                    .position(initialPosition)
                    .title("Текущая локация")
            )
        } else {
            // Центр по умолчанию (Москва)
            val defaultPosition = LatLng(55.7558, 37.6173)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 10f))
        }

        // Обработка кликов по карте
        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Выбранная локация")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}