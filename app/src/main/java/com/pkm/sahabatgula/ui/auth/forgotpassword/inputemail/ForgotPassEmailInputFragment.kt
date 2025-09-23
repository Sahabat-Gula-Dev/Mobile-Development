package com.pkm.sahabatgula.ui.auth.forgotpassword.inputemail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentEmailInputForgotPassBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPassEmailInputFragment : Fragment() {

    private var _binding : FragmentEmailInputForgotPassBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgotPasswordEmailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEmailInputForgotPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            val email = binding.editInputEmail.text.toString()
            viewModel.requestOtp(email)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.btnContinue.isEnabled = state !is ForgotPasswordEmailState.Loading
                    }
                }
                launch {
                    viewModel.effect.collect { effect ->
                        when(effect) {
                            is ForgotPasswordEmailEffect.ShowToast -> {
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_LONG).show()
                            }
                            is ForgotPasswordEmailEffect.NavigateToOtpVerification -> {
                                val bundle = bundleOf("email" to effect.email)
                                findNavController().navigate(R.id.action_forgot_pass_input_email_to_verify_otp, bundle)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}