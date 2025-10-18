package com.pkm.sahabatgula.ui.auth.forgotpassword.inputemail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.Validator
import com.pkm.sahabatgula.databinding.FragmentEmailInputForgotPassBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPassEmailInputFragment : Fragment() {

    private var _binding: FragmentEmailInputForgotPassBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgotPasswordEmailViewModel by viewModels()
    private var stateDialog: StateDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailInputForgotPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            val email = binding.inputEmail.editText?.text.toString().trim()
            val emailError = Validator.validateEmail(email)
            binding.inputEmail.error = emailError
            if (emailError == null) {
                viewModel.requestOtp(email)
            }
        }

        observeUiState()
        observeEffects()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ForgotPasswordEmailState.Idle -> hideStateDialog()
                        is ForgotPasswordEmailState.Loading -> showStateDialog(
                            GlobalUiState.Loading(
                                message = "Gluby sedang menganalisis data kamu, tunggu beberapa detik"
                            )
                        )
                        is ForgotPasswordEmailState.Success -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Gluby Sudah Kirim Kode OTP!",
                                    message = "Cek email kamu ya! Masukkan kode OTP yang kami kirim untuk lanjut ke tahap berikutnya."
                                )
                            )
                        }

                        is ForgotPasswordEmailState.Error -> {
                            hideStateDialog()
                            binding.inputEmail.error = state.message
                        }
                    }
                    binding.btnContinue.isEnabled = state !is ForgotPasswordEmailState.Loading
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is ForgotPasswordEmailEffect.ShowError -> {
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = "Terjadi Kesalahan",
                                    message = "Terjadi kesalahan saat memproses permintaan. Periksa kembali data yang kamu masukkan"
                                )
                            )
                        }
                        is ForgotPasswordEmailEffect.ShowInfo -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Berhasil",
                                    message = effect.message
                                )
                            )
                        }
                        is ForgotPasswordEmailEffect.NavigateToOtpVerification -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "OTP Terkirim!",
                                    message = "Cek email kamu ya! Masukkan kode OTP yang kami kirim untuk lanjut ke tahap berikutnya."
                                )
                            )
                            stateDialog?.dismissListener = {
                                val bundle = bundleOf("email" to effect.email)
                                findNavController().navigate(
                                    R.id.action_forgot_pass_input_email_to_verify_otp,
                                    bundle
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showStateDialog(state: GlobalUiState) {
        if (stateDialog?.dialog?.isShowing == true) stateDialog?.dismiss()
        stateDialog = StateDialogFragment.newInstance(state)
        stateDialog?.show(childFragmentManager, "StateDialog")
    }

    private fun hideStateDialog() {
        stateDialog?.dismiss()
        stateDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
