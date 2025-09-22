package com.pkm.sahabatgula.ui.auth.register.inputdatauser.historyfamily

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
import com.pkm.sahabatgula.data.remote.model.DiabetesFamily
import com.pkm.sahabatgula.databinding.FragmentActivityHistoryBinding
import com.pkm.sahabatgula.databinding.FragmentInputDataUserBloodPressureBinding
import com.pkm.sahabatgula.databinding.FragmentInputDataUserHistoryFamilyBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class InputDataUserHistoryFamilyFragment : Fragment() {

    private var _binding: FragmentInputDataUserHistoryFamilyBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputDataUserHistoryFamilyBinding.inflate(inflater, container, false)
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
                    updateDiabetesFamSelection(profileData.diabetesFamily)
                }
            }
        }
    }

    private fun updateDiabetesFamSelection(selectedDiabetesFam: String?) {
        val isFirstSelected = selectedDiabetesFam == DiabetesFamily.FIRSTFAM.value
        binding.chooseFirstFam.radioButton.isChecked = isFirstSelected
        binding.chooseFirstFam.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isFirstSelected) R.color.selected_card else R.color.md_theme_onPrimary)
        )
        binding.chooseFirstFam.tvTitleChoice.text = "Kerabat Tingkat Satu"
        binding.chooseFirstFam.tvSubtitleChoice.text = "Seperti ayah, ibu, atau saudara kandung. Risiko genetik lebih tinggi"
        binding.chooseFirstFam.tvSubtitleChoice.visibility =
            if (isFirstSelected) View.VISIBLE else View.GONE

        val isSecondSelected = selectedDiabetesFam == DiabetesFamily.SECONDFAM.value
        binding.chooseSecondFam.radioButton.isChecked = isSecondSelected
        binding.chooseSecondFam.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isSecondSelected) R.color.selected_card else R.color.md_theme_onPrimary)
        )
        binding.chooseSecondFam.tvTitleChoice.text = "Kerabat Tingkat Dua"
        binding.chooseSecondFam.tvSubtitleChoice.text = "Seperti kakek, nenek, paman, atau bibi. Risiko sedikit meningkat"
        binding.chooseSecondFam.tvSubtitleChoice.visibility =
            if (isSecondSelected) View.VISIBLE else View.GONE

        val isNoneSelected = selectedDiabetesFam == DiabetesFamily.NONE.value
        binding.chooseThirdFam.radioButton.isChecked = isNoneSelected
        binding.chooseThirdFam.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isNoneSelected) R.color.selected_card else R.color.md_theme_onPrimary)
        )
        binding.chooseThirdFam.tvTitleChoice.text = "Tidak Punya Riwayat"
        binding.chooseThirdFam.tvSubtitleChoice.text = "Risiko genetik rendah, terus pertahankan pola hidup sehat"
        binding.chooseThirdFam.tvSubtitleChoice.visibility =
            if (isNoneSelected) View.VISIBLE else View.GONE

    }

    private fun setupClickListener() {
        binding.chooseFirstFam.root.setOnClickListener {
            inputDataViewModel.selectDiabetesFamily(DiabetesFamily.FIRSTFAM.value)
        }
        binding.chooseSecondFam.root.setOnClickListener {
            inputDataViewModel.selectDiabetesFamily(DiabetesFamily.SECONDFAM.value)
        }
        binding.chooseThirdFam.root.setOnClickListener {
            inputDataViewModel.selectDiabetesFamily(DiabetesFamily.NONE.value)
        }

        binding.chooseFirstFam.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                inputDataViewModel.selectDiabetesFamily(DiabetesFamily.FIRSTFAM.value)
            }
        }

        binding.chooseSecondFam.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                inputDataViewModel.selectDiabetesFamily(DiabetesFamily.SECONDFAM.value)
            }
        }

        binding.chooseThirdFam.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                inputDataViewModel.selectDiabetesFamily(DiabetesFamily.NONE.value)
            }
        }

        binding.btnContinueToActivity.setOnClickListener {
            if(inputDataViewModel.profileData.value.diabetesFamily != null){
                findNavController().navigate(R.id.input_history_family_to_input_user_activity)
            }
            else {
                Toast.makeText(requireContext(), "Silakan Lengkapi Data Anda", Toast.LENGTH_SHORT).show()
            }
        }
    }


}