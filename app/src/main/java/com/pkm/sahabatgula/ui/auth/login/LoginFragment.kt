package com.pkm.sahabatgula.ui.auth.login

import android.os.Bundle
import android.util.Log
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
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = binding.editInputEmail
        val etPassword = binding.editInputPassword
        val btnLogin = binding.btnLogin

        btnLogin.setOnClickListener {
            loginViewModel.login(
                etEmail.text.toString(),
                etPassword.text.toString()
            )
        }

        binding.tvForgotPasswordClickable.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        observeState()
        observeEffect()

    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginState.collect { it ->
                when (it) {
                    is LoginViewState.Idle -> {
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewState.Loading -> {
                        binding.btnLogin.isEnabled = false
                        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_LONG).show()
                    }
                    is LoginViewState.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        Log.d("LoginFragment", "observeState: ${it.message}")
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewState.Success -> binding.btnLogin.isEnabled = false
                }
            }
        }
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                loginViewModel.effect.collect { effect ->
                    when(effect) {
                        is LoginEffect.ShowToast -> {
                            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_LONG).show()
                        }
                        is LoginEffect.NavigateToHome -> {
                            findNavController().navigate(R.id.action_login_to_input_data)
                        }
                    }
                }
            }
        }
    }

}