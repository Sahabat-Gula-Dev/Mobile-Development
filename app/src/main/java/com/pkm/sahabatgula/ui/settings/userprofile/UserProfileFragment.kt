package com.pkm.sahabatgula.ui.settings.userprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.getRiskCategory
import com.pkm.sahabatgula.databinding.FragmentUserProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserProfileViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfileState.collect { state ->
                when (state) {
                    is UserProfileState.Success -> {
                        binding.tvNumberHeight.text = state.height.toString()
                        binding.tvNumberWeight.text = state.weight.toString()
                        binding.tvNumberBmi.text = state.bmi.toString()
                        val riskIndex = state.diabetesRiskIndex
                        val riskCategory = getRiskCategory(requireContext(), riskIndex)
                        val numberRisk = binding.cardDiabetesRisk.tvNumberOfRisk
                        binding.cardDiabetesRisk.bgNumberOfRisk.setCardBackgroundColor(riskCategory.colorRes)
                        numberRisk.text = String.format("%02d", riskIndex)
                        binding.cardDiabetesRisk.tvTitleIndexRisk.text = riskCategory.title
                        binding.cardDiabetesRisk.subtitleTvIndexRisk.text = riskCategory.subtitle

                    }
                    is UserProfileState.Loading -> {}
                    is UserProfileState.Error -> {
                    }
                }
            }

        }

        binding.cardReAssessment.apply {
            icCardSetting.setImageResource(R.drawable.ic_assignment)
            tvTitleSettingCard.text = "Asesmen Kesehatan Ulang"
            tvSubtitleSettingCard.text = "Perbarui data risiko kesehatanmu"

            root.setOnClickListener {
                findNavController().navigate(R.id.action_user_profile_fragment_to_input_data_graph)
            }
        }
    }

    //destroy
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}