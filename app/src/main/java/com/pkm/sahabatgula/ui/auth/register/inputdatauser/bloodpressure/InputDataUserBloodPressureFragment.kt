package com.pkm.sahabatgula.ui.auth.register.inputdatauser.bloodpressure

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputDataUserBloodPressureBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch

class InputDataUserBloodPressureFragment : Fragment() {

    private var _binding: FragmentInputDataUserBloodPressureBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputDataUserBloodPressureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { profileData ->

                    // update pilihan yes/no
                    when (profileData.bloodPressure) {
                        true -> updateBloodPressureSelection(true)
                        false -> updateBloodPressureSelection(false)
                        null -> resetBloodPressureSelection() // handle default state
                    }

                    // update tombol continue
                    binding.btnContinueToHighBloodGlucose.isEnabled =
                        profileData.bloodPressure != null
                }
            }
        }
    }

    private fun resetBloodPressureSelection() {
        // Semua reset ke default
        binding.choseYesBloodPressure.radioButton.isChecked = false
        binding.choseYesBloodPressure.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.choseYesBloodPressure.tvSubtitleChoice.visibility = View.GONE

        binding.choseNoBloodPressure.radioButton.isChecked = false
        binding.choseNoBloodPressure.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.choseNoBloodPressure.tvSubtitleChoice.visibility = View.GONE
    }

    private fun updateBloodPressureSelection(selectedBloodPressure: Boolean) {
        val isYesSelected = selectedBloodPressure
        binding.choseYesBloodPressure.radioButton.isChecked = isYesSelected
        binding.choseYesBloodPressure.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isYesSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.choseYesBloodPressure.tvSubtitleChoice.visibility =
            if (isYesSelected) View.VISIBLE else View.GONE

        val isNoSelected = !selectedBloodPressure
        binding.choseNoBloodPressure.radioButton.isChecked = isNoSelected
        binding.choseNoBloodPressure.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isNoSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.choseNoBloodPressure.tvSubtitleChoice.visibility =
            if (isNoSelected) View.VISIBLE else View.GONE


    }

    private fun setupClickListener() {
        binding.choseYesBloodPressure.tvTitleChoice.text = "Pernah Atau Sedang Mengalami"
        binding.choseNoBloodPressure.tvTitleChoice.text = "Tidak Pernah Mengalami"

        val bloodPressure = inputDataViewModel.profileData.value.bloodPressure
        binding.choseYesBloodPressure.root.setOnClickListener {
            inputDataViewModel.selectBloodPressure(true)
        }
        binding.choseNoBloodPressure.root.setOnClickListener {
            inputDataViewModel.selectBloodPressure(false)
        }

        binding.btnContinueToHighBloodGlucose.isEnabled = bloodPressure != null
        binding.btnContinueToHighBloodGlucose.setOnClickListener {
            val currentBloodPressure = inputDataViewModel.profileData.value.bloodPressure
            if (currentBloodPressure != null) {
                findNavController().navigate(R.id.input_blood_pressure_to_input_high_blood_glucose)
            } else {
                Toast.makeText(requireContext(), "Silakan memilih terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

}