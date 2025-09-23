package com.pkm.sahabatgula.ui.auth.forgotpassword.verifyotp

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentOtpVerificationBinding
import com.pkm.sahabatgula.ui.auth.otpverification.OtpEffect
import com.pkm.sahabatgula.ui.auth.otpverification.OtpViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyResetOtpFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VerifyOtpForgotPassViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailFromArgs = arguments?.getString("email")
        if (emailFromArgs != null) {
            viewModel.setEmail(emailFromArgs)
        } else {
            Toast.makeText(requireContext(), "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        binding.btnVerify.setOnClickListener {
            val email = viewModel.email
            val otp = binding.editInputOtp.text.toString()
            Log.d("OTP", "Verify clicked code='$otp'")
            if (otp.isNotEmpty()) viewModel.verifyOtpReset(email, otp)
        }

        binding.tvResend.setOnClickListener {
            if (viewModel.uiState.value is OtpForgotPassState.ReadyToResend) {
                viewModel.resendOtp()
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is OtpForgotPassState.Ticking -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text =
                                    getString(R.string.resend_in_30_secs, state.remaining)
                                binding.btnVerify.isEnabled = binding.editInputOtp.text?.length == 6
                            }

                            OtpForgotPassState.ReadyToResend -> {
                                binding.tvResend.isEnabled = true
                                binding.tvResend.text = getString(R.string.resend)
                                binding.tvResend.setTypeface(null, Typeface.BOLD)
                                binding.btnVerify.isEnabled = binding.editInputOtp.text?.length == 6
                            }

                            OtpForgotPassState.Loading -> {
                                binding.btnVerify.isEnabled = false
                            }

                            OtpForgotPassState.Idle -> Unit
                        }
                    }
                }
                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is OtpForgotPassEffect.ShowToast -> {
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is OtpForgotPassEffect.VerificationSuccess -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Verifikasi berhasil!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val resetToken = effect.resetToken
                                val bundle = Bundle()
                                bundle.putString("resetToken", resetToken)
                                // Navigasi ke fragment untuk input password baru
                                findNavController().navigate(R.id.action_verify_otp_to_reset_password, bundle)
                            }
                        }
                    }
                }
            }
        }
    }

}