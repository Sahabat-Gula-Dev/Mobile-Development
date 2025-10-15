package com.pkm.sahabatgula.ui.auth.forgotpassword.verifyotp

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentOtpVerificationBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyResetOtpFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VerifyOtpForgotPassViewModel>()
    private var stateDialog: StateDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getString("email")?.let {
            viewModel.email = it
        }
        if (viewModel.ui.value is OtpForgotPassViewState.Idle) viewModel.startTimer()
        binding.editInputOtp.doAfterTextChanged {
            binding.btnVerify.isEnabled = (it?.length == 6)
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.editInputOtp.text?.toString()?.trim().orEmpty()
            showStateDialog(GlobalUiState.Loading("Tunggu sebentar..."))
            viewModel.verify(code)
        }

        binding.tvResend.setOnClickListener {
            if (viewModel.ui.value is OtpForgotPassViewState.ReadyToResend) {
                showStateDialog(GlobalUiState.Loading("Mengirim ulang kode"))
                viewModel.resendOtp()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                val customFont = ResourcesCompat.getFont(requireContext(), R.font.jakarta_sans_family)
                launch {
                    viewModel.ui.collect { state ->
                        when (state) {
                            is OtpForgotPassViewState.Idle -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = getString(R.string.resend_in_30_secs)
                                binding.tvResend.setTypeface(customFont)
                            }
                            is OtpForgotPassViewState.Ticking -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = "Kirim ulang dalam ${state.remaining} detik"
                                binding.tvResend.setTypeface(customFont)
                            }
                            is OtpForgotPassViewState.ReadyToResend -> {
                                binding.tvResend.isEnabled = true
                                binding.tvResend.setTypeface(customFont, Typeface.BOLD)
                                binding.tvResend.text = getString(R.string.otp_resend_ready)
                            }
                            is OtpForgotPassViewState.Loading -> {
                                binding.tvResend.isEnabled = false
                                binding.tvResend.text = "Mengirim ulang..."
                            }
                        }
                    }
                }


                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is OtpForgotPassEffect.ShowVerifyError -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Error(
                                        title = "Verifikasi Gagal",
                                        message = "Kode OTP yang kamu masukkan salah atau sudah kadaluarsa. Coba kirim ulang kode."
                                    )
                                )
                            }
                            is OtpForgotPassEffect.ShowResendInfo -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Success(
                                        title = "Kode telah dikirim ulang",
                                        message = "Gluby telah mengirim kode OTP baru. Cek Email kamu dan lanjutkan verifikasi."
                                    )
                                )
                            }
                            is OtpForgotPassEffect.VerificationSuccess -> {
                                stateDialog?.updateState(
                                    GlobalUiState.Success(
                                        title = "Verifikasi Berhasil",
                                        message = "Silakan buat kata sandi baru untuk akunmu."
                                    )
                                )
                                stateDialog?.dismissListener = {
                                    val bundle = Bundle().apply {
                                        putString("resetToken", effect.resetToken)
                                    }
                                    findNavController().navigate(
                                        R.id.action_verify_otp_to_reset_password,
                                        bundle
                                    )
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
