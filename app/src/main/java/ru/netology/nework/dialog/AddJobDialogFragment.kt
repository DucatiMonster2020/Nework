package ru.netology.nework.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddJobDialogFragment(
    private val onSave: (company: String, position: String, startDate: String, endDate: String?, isCurrent: Boolean) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddJobBinding? = null
    private val binding get() = _binding!!

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var isCurrentJob = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddJobBinding.inflate(layoutInflater)

        setupClickListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.add_job)
            .setPositiveButton(R.string.save) { _, _ ->
                saveJob()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun setupClickListeners() {
        binding.startDateButton.setOnClickListener {
            showDatePicker(true)
        }

        binding.endDateButton.setOnClickListener {
            showDatePicker(false)
        }

        binding.currentJobCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isCurrentJob = isChecked
            binding.endDateButton.isEnabled = !isChecked
            binding.endDateLabel.isEnabled = !isChecked
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                val formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

                if (isStartDate) {
                    startDate = date
                    binding.startDateButton.text = formattedDate
                } else {
                    endDate = date
                    binding.endDateButton.text = formattedDate
                }
            },
            year,
            month,
            day
        ).show()
    }

    private fun saveJob() {
        val company = binding.companyEditText.text.toString()
        val position = binding.positionEditText.text.toString()

        if (company.isBlank()) {
            Toast.makeText(requireContext(), "Введите название компании", Toast.LENGTH_SHORT).show()
            return
        }

        if (position.isBlank()) {
            Toast.makeText(requireContext(), "Введите должность", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDate == null) {
            Toast.makeText(requireContext(), "Выберите дату начала работы", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isCurrentJob && endDate == null) {
            Toast.makeText(requireContext(), "Выберите дату окончания работы", Toast.LENGTH_SHORT).show()
            return
        }

        val startDateStr = startDate!!.toString()
        val endDateStr = if (!isCurrentJob) endDate!!.toString() else null

        onSave(company, position, startDateStr, endDateStr, isCurrentJob)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}