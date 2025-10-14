package com.pkm.sahabatgula.ui.auth.register.inputdatauser.highbloodglucose

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
import com.pkm.sahabatgula.databinding.FragmentInputDataUserHighBloodGlucoseBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch


class InputDataUserHighBloodGluFragment : Fragment() {

    private var _binding: FragmentInputDataUserHighBloodGlucoseBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputDataUserHighBloodGlucoseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (inputDataViewModel.profileData.value.bloodSugar == null) {
            inputDataViewModel.selectHighBloodGlucose(true)
        }

        setupClickListener()
        observeViewModel()
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                inputDataViewModel.profileData.collect { profileData ->
                    when (profileData.bloodSugar) {
                        true -> updateBloodGlucose(true)
                        false -> updateBloodGlucose(false)
                        null -> resetBloodGlucose()
                    }

                    binding.btnContinueToConsumption.isEnabled = profileData.bloodSugar != null
                }
            }
        }
    }

    private fun resetBloodGlucose() {
        binding.chooseYesBloodGlucose.radioButton.isChecked = false
        binding.chooseYesBloodGlucose.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.chooseYesBloodGlucose.tvSubtitleChoice.text = "Gula darah tinggi bisa jadi tanda awal gangguan metabolisme."
        binding.chooseYesBloodGlucose.tvSubtitleChoice.visibility = View.GONE

        binding.chooseNoBloodGlucose.radioButton.isChecked = false
        binding.chooseNoBloodGlucose.tvSubtitleChoice.text = "Sangat penting untuk melakukan cek gula darah secara rutin."
        binding.chooseNoBloodGlucose.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.chooseNoBloodGlucose.tvSubtitleChoice.visibility = View.GONE
    }

    private fun updateBloodGlucose(selectBloodGlucose: Boolean) {
        val isYesSelected = selectBloodGlucose
        binding.chooseYesBloodGlucose.radioButton.isChecked = isYesSelected
        binding.chooseYesBloodGlucose.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if(isYesSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.chooseYesBloodGlucose.tvSubtitleChoice.visibility =
            if(isYesSelected) View.VISIBLE
            else View.GONE

        val isNoSelected = !selectBloodGlucose
        binding.chooseNoBloodGlucose.radioButton.isChecked = isNoSelected
        binding.chooseNoBloodGlucose.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if(isNoSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.chooseNoBloodGlucose.tvSubtitleChoice.visibility =
            if(isNoSelected) View.VISIBLE
            else View.GONE

    }

    private fun setupClickListener() {
        binding.chooseYesBloodGlucose.tvTitleChoice.text = "Pernah Atau Sedang Mengalami"
        binding.chooseNoBloodGlucose.tvSubtitleChoice.text = "Gula darah tinggi bisa jadi tanda awal gangguan metabolisme."
        binding.chooseNoBloodGlucose.tvTitleChoice.text = "Tidak Pernah Mengalami"
        binding.chooseNoBloodGlucose.tvSubtitleChoice.text = "Sangat penting untuk melakukan cek gula darah secara rutin."

        val bloodGlucose = inputDataViewModel.profileData.value.bloodSugar
        binding.chooseYesBloodGlucose.root.setOnClickListener {
            inputDataViewModel.selectHighBloodGlucose(true)
        }
        binding.chooseNoBloodGlucose.root.setOnClickListener {
            inputDataViewModel.selectHighBloodGlucose(false)
        }

        binding.btnContinueToConsumption.isEnabled = bloodGlucose != null
        binding.btnContinueToConsumption.setOnClickListener {
            val currentBloodGlucose = inputDataViewModel.profileData.value.bloodSugar
            if(currentBloodGlucose !=null) {
                findNavController().navigate(R.id.input_high_blood_glucose_to_input_daily_consumption)
            } else {
                Toast.makeText(requireContext(), "Silakan Melengkapi Data Anda", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (inputDataViewModel.profileData.value.bloodSugar == null) {
            inputDataViewModel.selectHighBloodGlucose(true)
        }
    }

}