package com.pkm.sahabatgula.ui.auth.otpverification

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentOtpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationRegisterFragment : Fragment() {

    private val otpViewModel: OtpVerificationRegisterViewModel by viewModels()

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtpVerificationBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(otpViewModel.ui.value is OtpViewState.Idle) otpViewModel.startTimer()

        binding.editInputOtp.doAfterTextChanged {
            binding.btnVerify.isEnabled=(it?.length == 6)
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.editInputOtp.text?.toString()?.trim().orEmpty()
            val email = otpViewModel.email
            Log.d("OTP", "Verify clicked code='$code'")
            otpViewModel.verify(email, code)
        }

        binding.tvResend.setOnClickListener {
            if(otpViewModel.ui.value is OtpViewState.ReadyToResend){
                otpViewModel.resendOtp()
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    otpViewModel.ui.collect { state ->
                        when (state) {
                            is OtpViewState.Ticking -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = getString(R.string.resend_in_30_secs, state.remaining)
                                binding.btnVerify.isEnabled = binding.editInputOtp.text?.length == 6
                            }
                            OtpViewState.ReadyToResend -> {
                                binding.tvResend.isEnabled = true
                                binding.tvResend.text = getString(R.string.resend)
                                binding.tvResend.setTypeface(null, Typeface.BOLD)
                                binding.btnVerify.isEnabled = binding.editInputOtp.text?.length == 6
                            }
                            OtpViewState.Loading -> {
                                binding.btnVerify.isEnabled = false
                            }
                            OtpViewState.Idle -> Unit
                        }
                    }
                }
                launch {
                    otpViewModel.effect.collect { effect ->
                        when (effect) {
                            is OtpEffect.ShowToast -> {
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is OtpEffect.VerificationSuccess -> {
                                val tokenManager = TokenManager(requireContext())
                                tokenManager.saveAccessToken(effect.accessToken)
                                Log.d("OTP", "Token saved: ${tokenManager.getAccessToken()}")
                                findNavController().navigate(R.id.otp_to_welcome_screen)
                            }
                            // TAMBAHKAN CABANG 'ELSE' DI SINI
                            else -> {
                                // Biarkan kosong jika tidak ada aksi lain yang perlu dilakukan
                                // Atau bisa untuk logging jika ada effect tak terduga
                                // android.util.Log.d("OTP", "Unhandled effect: $effect")
                            }
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
