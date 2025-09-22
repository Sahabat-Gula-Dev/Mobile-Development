package com.pkm.sahabatgula.ui.auth.register.inputdatauser.activity

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
import com.pkm.sahabatgula.data.remote.model.ActivityLevel
import com.pkm.sahabatgula.databinding.FragmentInputDataUserActivityBinding
import com.pkm.sahabatgula.databinding.FragmentInputDataUserGenderBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class InputDataUserActivityFragment : Fragment() {

    private var _binding: FragmentInputDataUserActivityBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputDataUserActivityBinding.inflate(inflater, container, false)
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
                    updateActivitySelection(profileData.activityLevel)
                }
            }
        }
    }

    private fun updateActivitySelection(selectedActivityLevel: String?){
        val isInactive = selectedActivityLevel == ActivityLevel.INACTIVE.value
        binding.chooseInactive.radioButton.isChecked = isInactive
        binding.chooseInactive.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isInactive) R.color.selected_card else R.color.md_theme_onPrimary
            )
        )
        binding.chooseInactive.tvTitleChoice.text = "Tidak Aktif Beraktivita"
        binding.chooseInactive.tvSubtitleChoice.text = "Kebanyakan duduk, jarang untuk bergerak atau olahraga"
        binding.chooseInactive.tvSubtitleChoice.visibility =
            if(isInactive) View.VISIBLE else View.GONE

        val isLightlyActive = selectedActivityLevel == ActivityLevel.LIGHTLY_ACTIVE.value
        binding.chooseLightlyActive.radioButton.isChecked = isLightlyActive
        binding.chooseLightlyActive.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isLightlyActive) R.color.selected_card else R.color.md_theme_onPrimary
            )
        )
        binding.chooseLightlyActive.tvTitleChoice.text = "Aktivitas Ringan"
        binding.chooseLightlyActive.tvSubtitleChoice.text = "Sesekali jalan kaki atau kerja ringan, tapi belum rutin"
        binding.chooseLightlyActive.tvSubtitleChoice.visibility =
            if(isLightlyActive) View.VISIBLE else View.GONE

        val isModeratelyActive = selectedActivityLevel == ActivityLevel.MODERATELY_ACTIVE.value
        binding.chooseModeratelyActive.radioButton.isChecked = isModeratelyActive
        binding.chooseModeratelyActive.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isModeratelyActive) R.color.selected_card else R.color.md_theme_onPrimary
            )
        )
        binding.chooseModeratelyActive.tvTitleChoice.text = "Aktivitas Sedang"
        binding.chooseModeratelyActive.tvSubtitleChoice.text = "Rutin jalan kaki, bersepeda santai, atau kerja fisik ringan"
        binding.chooseModeratelyActive.tvSubtitleChoice.visibility =
            if(isModeratelyActive) View.VISIBLE else View.GONE

        val isVeryActive = selectedActivityLevel == ActivityLevel.VERY_ACTIVE.value
        binding.chooseVeryActive.radioButton.isChecked = isVeryActive
        binding.chooseVeryActive.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isVeryActive) R.color.selected_card else R.color.md_theme_onPrimary
            )
        )
        binding.chooseVeryActive.tvTitleChoice.text = "Aktivitas Berat"
        binding.chooseVeryActive.tvSubtitleChoice.text = "Olahraga rutin atau kerja fisik cukup berat setiap minggu"
        binding.chooseVeryActive.tvSubtitleChoice.visibility =
            if(isVeryActive) View.VISIBLE else View.GONE


        val isExtremelyActive = selectedActivityLevel == ActivityLevel.EXTREMELY_ACTIVE.value
        binding.chooseExtremelyActive.radioButton.isChecked = isExtremelyActive
        binding.chooseExtremelyActive.cardChoice.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(),
                if (isExtremelyActive) R.color.selected_card else R.color.md_theme_onPrimary
            )
        )
        binding.chooseExtremelyActive.tvTitleChoice.text = "Aktivitas Sangat Berat"
        binding.chooseExtremelyActive.tvSubtitleChoice.text = "Kerja fisik berat atau olahraga intens hampir setiap hari"
        binding.chooseExtremelyActive.tvSubtitleChoice.visibility =
            if(isExtremelyActive) View.VISIBLE else View.GONE

    }

    private fun setupClickListener() {
        binding.chooseInactive.root.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.INACTIVE.value)
        }

        binding.chooseLightlyActive.root.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.LIGHTLY_ACTIVE.value)
        }

        binding.chooseModeratelyActive.root.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.MODERATELY_ACTIVE.value)
        }

        binding.chooseVeryActive.root.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.VERY_ACTIVE.value)
        }

        binding.chooseExtremelyActive.root.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.EXTREMELY_ACTIVE.value)
        }


        binding.chooseInactive.radioButton.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.INACTIVE.value)
        }

        binding.chooseLightlyActive.radioButton.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.LIGHTLY_ACTIVE.value)
        }

        binding.chooseModeratelyActive.radioButton.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.MODERATELY_ACTIVE.value)
        }

        binding.chooseVeryActive.radioButton.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.VERY_ACTIVE.value)
        }

        binding.chooseExtremelyActive.radioButton.setOnClickListener {
            inputDataViewModel.selectActivityLevel(ActivityLevel.EXTREMELY_ACTIVE.value)
        }

        binding.btnSubmitData.setOnClickListener {
            if (inputDataViewModel.profileData.value.activityLevel != null) {
                inputDataViewModel.submitProfileData()
                Toast.makeText(requireContext(),"Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_input_data_user_activity_to_home)
            }
        }


    }
}