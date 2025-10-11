package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.Validator
import com.pkm.sahabatgula.databinding.FragmentResetPasswordBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResetPasswordViewModel by viewModels()

    private var stateDialog: StateDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChangePassword.setOnClickListener {
            val resetToken = requireArguments().getString("resetToken") ?: return@setOnClickListener

            val newPassword = binding.editInputNewPass.text.toString()
            val confirmPassword = binding.editConfirmPass.text.toString()

            // ✅ Validasi pakai Validator
            val passwordError = Validator.validatePassword(newPassword)
            val confirmError = if (newPassword != confirmPassword) "Konfirmasi password tidak cocok" else null

            binding.inputNewPass.error = passwordError
            binding.inputConfirmPass.error = confirmError

            if (passwordError == null && confirmError == null) {
                viewModel.resetPassword(resetToken, newPassword, confirmPassword)
            }
        }

        observeUiState()
        observeEffect()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ResetPasswordState.Idle -> hideStateDialog()

                        is ResetPasswordState.Loading -> showStateDialog(
                            GlobalUiState.Loading(
                                message = "Gluby sedang mengatur ulang password kamu..."
                            )
                        )

                        is ResetPasswordState.Success -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Yey! Password Baru!",
                                    message = "Sekarang kamu bisa login dengan password barumu dan jangan lupa simpan dengan baik"
                                )
                            )
                            stateDialog?.dismissListener = {
                                viewModel.resetPassword("", "", "")
                                findNavController().navigate(R.id.action_reset_password_to_login)
                            }
                        }

                        is ResetPasswordState.Error -> {
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = "Oops, Ada Masalah",
                                    message = "Pastikan password baru dan konfirmasinya sama persis, lalu coba lagi"
                                )
                            )
                        }
                    }

                    binding.btnChangePassword.isEnabled = state !is ResetPasswordState.Loading
                }
            }
        }
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is ResetPasswordEffect.NavigateToLogin -> {
                            // Navigasi sudah ditangani lewat dialog dismissListener
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
