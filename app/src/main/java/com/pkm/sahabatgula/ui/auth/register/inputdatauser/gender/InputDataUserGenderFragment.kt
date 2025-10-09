package com.pkm.sahabatgula.ui.auth.register.inputdatauser.gender

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.remote.model.Gender
import com.pkm.sahabatgula.databinding.FragmentInputDataUserGenderBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InputDataUserGenderFragment : Fragment() {

    private var _binding: FragmentInputDataUserGenderBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputDataUserGenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (inputDataViewModel.profileData.value.gender == null) {
            inputDataViewModel.selectGender(Gender.MALE.value)
        }

        setupClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { profileData ->
                    updateGenderSelection(profileData.gender)
                }
            }
        }
    }

    private fun updateGenderSelection(selectedGender: String?) {
        val isMaleSelected = selectedGender == Gender.MALE.value
        binding.cardMale.radioButton.isChecked = isMaleSelected
        binding.cardMale.cardGender.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isMaleSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.cardMale.tvGender.text = "Laki-laki"
        binding.cardMale.tvCardSubtitleInputGender.visibility =
            if (isMaleSelected) View.VISIBLE else View.GONE

        val isFemaleSelected = selectedGender == Gender.FEMALE.value
        binding.cardFemale.radioButton.isChecked = isFemaleSelected
        binding.cardFemale.cardGender.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isFemaleSelected) R.color.selected_card
                else R.color.md_theme_onPrimary
            )
        )
        binding.cardFemale.tvGender.text = "Perempuan"
        binding.cardFemale.iconGender.setImageResource(R.drawable.ic_female)
        binding.cardFemale.tvCardSubtitleInputGender.text = "Kebutuhan kalori juga dipengaruhi oleh hormon"
        binding.cardFemale.tvCardSubtitleInputGender.visibility =
            if (isFemaleSelected) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListener() {
        binding.cardMale.root.setOnClickListener {
            inputDataViewModel.selectGender(Gender.MALE.value)
        }
        binding.cardFemale.root.setOnClickListener {
            inputDataViewModel.selectGender(Gender.FEMALE.value)
        }

        // jiak dipilih male, nilainya laki-laki
        binding.cardMale.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputDataViewModel.selectGender(Gender.MALE.value)
            }
        }
        binding.cardFemale.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputDataViewModel.selectGender(Gender.FEMALE.value)
            }
        }
        binding.btnContinueToAge.setOnClickListener {
            val currentGender = inputDataViewModel.profileData.value.gender
            if(currentGender != null) {
                findNavController().navigate(R.id.input_gender_to_input_age)
            } else {
                Toast.makeText(requireContext(), "Silakan pilih jenis kelamin Anda", Toast.LENGTH_SHORT).show()

            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}