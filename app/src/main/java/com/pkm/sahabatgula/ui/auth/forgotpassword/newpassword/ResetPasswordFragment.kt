package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentResetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResetPasswordViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChangePassword.setOnClickListener {
            val resetToken = requireArguments().getString("resetToken")
            if (resetToken != null) {
                val newPassword = binding.editInputNewPass.text.toString()
                val confirmPassword = binding.editConfirmPass.text.toString()
                viewModel.resetPassword(resetToken, newPassword, confirmPassword)
            } else {
                Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.btnChangePassword.isEnabled = state !is ResetPasswordState.Loading

                }
            }
                launch {
                    viewModel.uiEffect.collect { effect ->
                        when (effect) {
                            is ResetPasswordEffect.ShowToast -> {
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is ResetPasswordEffect.NavigateToLogin -> {
                                findNavController().navigate(R.id.action_reset_password_to_login)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}