package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val args: MapFragmentArgs by navArgs()

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
        displayCoordinates()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setTitle(R.string.location)
        }
    }

    private fun displayCoordinates() {
        val latitude = args.latitude
        val longitude = args.longitude

        if (latitude == 0.0 && longitude == 0.0) {
            binding.coordinatesText.text = "Координаты не указаны"
            binding.openInMapsButton.isEnabled = false
            binding.copyButton.isEnabled = false
        } else {
            binding.coordinatesText.text =
                "Широта: ${String.format("%.6f", latitude)}\n" +
                        "Долгота: ${String.format("%.6f", longitude)}"
        }
    }

    private fun setupClickListeners() {
        binding.openInMapsButton.setOnClickListener {
            openInExternalMaps()
        }

        binding.copyButton.setOnClickListener {
            copyCoordinatesToClipboard()
        }
    }

    private fun openInExternalMaps() {
        val latitude = args.latitude
        val longitude = args.longitude

        // Пробуем разные варианты
        val uris = listOf(
            "geo:$latitude,$longitude?q=$latitude,$longitude", // Стандартный
            "https://maps.google.com/?q=$latitude,$longitude", // Google Maps web
            "https://yandex.ru/maps/?pt=$longitude,$latitude&z=15" // Яндекс.Карты web
        )

        var opened = false
        for (uriString in uris) {
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(uriString)
                )
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                    opened = true
                    break
                }
            } catch (e: Exception) {
                continue
            }
        }

        if (!opened) {
            showNoMapsAppDialog()
        }
    }

    private fun copyCoordinatesToClipboard() {
        val latitude = args.latitude
        val longitude = args.longitude
        val coordinates = "$latitude, $longitude"

        val clipboard = android.content.ClipboardManager.getInstance(requireContext())
        val clip = android.content.ClipData.newPlainText("Координаты", coordinates)
        clipboard.setPrimaryClip(clip)

        android.widget.Toast.makeText(
            requireContext(),
            "Координаты скопированы в буфер обмена",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showNoMapsAppDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Карты не установлены")
            .setMessage("Установите приложение карт (Google Maps, Яндекс.Карты) для просмотра местоположения")
            .setPositiveButton("ОК", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}