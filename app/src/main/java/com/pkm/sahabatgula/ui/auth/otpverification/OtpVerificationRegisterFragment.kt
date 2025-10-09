package com.pkm.sahabatgula.ui.auth.otpverification

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentOtpVerificationBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationRegisterFragment : Fragment() {

    private val otpViewModel: OtpVerificationRegisterViewModel by viewModels()

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!
    private var stateDialog: StateDialogFragment? = null


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
            showStateDialog(GlobalUiState.Loading("Tunggu Sebentar"))
            otpViewModel.verify(email, code)
        }

        binding.tvResend.setOnClickListener {
            if(otpViewModel.ui.value is OtpViewState.ReadyToResend){
                showStateDialog(GlobalUiState.Loading("Mengirim ulang OTP"))
                otpViewModel.resendOtp()
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                val customFont = ResourcesCompat.getFont(requireContext(), R.font.jakarta_sans_family)
                // 1. Observe UI state → Timer dan resend
                launch {
                    otpViewModel.ui.collect { state ->
                        when (state) {
                            is OtpViewState.Idle -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = getString(R.string.resend_in_30_secs)
                                binding.tvResend.setTypeface(customFont)
                            }
                            is OtpViewState.Ticking -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = "Kirim ulang dalam ${state.remaining}s"
                                binding.tvResend.setTypeface(customFont)
                            }
                            is OtpViewState.ReadyToResend -> {
                                binding.tvResend.isEnabled = true
                                binding.tvResend.setTypeface(customFont, Typeface.BOLD)
                                binding.tvResend.text = getString(R.string.otp_resend_ready)
                            }
//                            is OtpViewState.Loading -> {
//                                binding.tvResend.isEnabled = false
//                                binding.tvResend.text = "Mengirim ulang..."
//                            }
                        }
                    }
                }

                // 2. Observe Effect → Toast & navigasi
                launch {
                    otpViewModel.effect.collect { effect ->
                        when (effect) {
                            is OtpEffect.ShowVerifyError -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Error(
                                        title = "Verifikasi Gagal",
                                        message = "Kode OTP yang kamu masukkan salah atau sudah kadaluarsa. Coba kirim ulang kode."
                                    )
                                )
                            }
                            is OtpEffect.ShowResendInfo -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Success(
                                        title = "Kode telah dikirim ulang",
                                        message = "Gluby telah berhasil mengirim ulang kode verifikasi, silakan buka email kamu untuk melakukan verifikasi."
                                    )
                                )
                            }
                            is OtpEffect.VerificationSuccess -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Success(
                                        title = "Verifikasi Berhasil",
                                        message = "Akun kamu sudah aktif dan siap digunakan. Terima kasih sudah bergabung."
                                    )
                                )

                                val tokenManager = TokenManager(requireContext())
                                tokenManager.saveAccessToken(effect.accessToken)
                                Log.d("OTP", "Token saved: ${tokenManager.getAccessToken()}")

                                stateDialog?.dismissListener = {
                                    findNavController().navigate(R.id.otp_to_welcome_screen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showStateDialog(state: GlobalUiState) {
        if (stateDialog?.dialog?.isShowing == true) {
            stateDialog?.dismiss()
        }
        stateDialog = StateDialogFragment.newInstance(state)
        stateDialog?.show(childFragmentManager, "StateDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
