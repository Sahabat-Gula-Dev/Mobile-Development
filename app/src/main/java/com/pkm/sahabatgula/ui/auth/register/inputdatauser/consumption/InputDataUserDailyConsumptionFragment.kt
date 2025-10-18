package com.pkm.sahabatgula.ui.auth.register.inputdatauser.consumption

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputDataUserDailyConsumptionBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch


class InputDataUserDailyConsumptionFragment : Fragment() {

    private var _binding: FragmentInputDataUserDailyConsumptionBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputDataUserDailyConsumptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (inputDataViewModel.profileData.value.eatVegetables == null) {
            inputDataViewModel.selectDailyConsumption(true)
        }

        setupClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { profileData ->

                    when (profileData.eatVegetables) {
                        true -> updateConsumptionSelection(true)
                        false -> updateConsumptionSelection(false)
                        null -> resetConsumptionSelection()
                    }

                    binding.btnContinueToHistoryFamily.isEnabled =
                        profileData.eatVegetables != null
                }
            }
        }
    }

    private fun setupClickListener() {
        binding.chooseYesConsumption.tvTitleChoice.text = "Rutin Mengonsumsi Setiap Hari"
        binding.chooseYesConsumption.tvSubtitleChoice.text =
            "Konsumsi 5-7 kali per minggu bantu jaga gula dan metabolisme."

        binding.chooseNoConsumption.tvTitleChoice.text = "Tidak Rutin Mengonsumsi"
        binding.chooseNoConsumption.tvSubtitleChoice.text =
            "Kurang dari 5 kali per minggu memicu fluktuasi gula dan ganggu pencernaan."


        val consumption = inputDataViewModel.profileData.value.eatVegetables
        binding.chooseYesConsumption.root.setOnClickListener {
            inputDataViewModel.selectDailyConsumption(true)
        }
        binding.chooseNoConsumption.root.setOnClickListener {
            inputDataViewModel.selectDailyConsumption(false)
        }
        binding.btnContinueToHistoryFamily.isEnabled = consumption != null
        binding.btnContinueToHistoryFamily.setOnClickListener {
            val currentConsumption = inputDataViewModel.profileData.value.eatVegetables
            if (currentConsumption!=null) {
            findNavController().navigate(R.id.input_daily_consumption_to_input_history_family)
            }
                else {
//                Toast.makeText(requireContext(), "Mohon Lengkapi Data Anda", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun resetConsumptionSelection() {
        binding.chooseYesConsumption.radioButton.isChecked = false
        binding.chooseYesConsumption.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.chooseYesConsumption.tvSubtitleChoice.visibility = View.GONE

        binding.chooseNoConsumption.radioButton.isChecked = false
        binding.chooseNoConsumption.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        binding.chooseNoConsumption.tvSubtitleChoice.visibility = View.GONE
    }

    private fun updateConsumptionSelection(selectedConsumption: Boolean) {
        val isYesSelected = selectedConsumption
        binding.chooseYesConsumption.radioButton.isChecked = isYesSelected
        binding.chooseYesConsumption.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isYesSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.chooseYesConsumption.tvSubtitleChoice.visibility =
            if (isYesSelected) View.VISIBLE else View.GONE

        val isNoSelected = !isYesSelected
        binding.chooseNoConsumption.radioButton.isChecked = isNoSelected
        binding.chooseNoConsumption.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isNoSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.chooseNoConsumption.tvSubtitleChoice.visibility =
            if (isNoSelected) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (inputDataViewModel.profileData.value.eatVegetables == null) {
            inputDataViewModel.selectDailyConsumption(true)
        }
    }


}