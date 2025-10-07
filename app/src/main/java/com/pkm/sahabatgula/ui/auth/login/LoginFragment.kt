package com.pkm.sahabatgula.ui.auth.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by viewModels<LoginViewModel>()

    @Inject lateinit var sessionManager: com.pkm.sahabatgula.data.local.SessionManager
    @Inject
    lateinit var apiService: com.pkm.sahabatgula.data.remote.api.ApiService


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

        val compSignInWithGoogle = binding.compSignInWithGoogle

        compSignInWithGoogle.btnGoogleSignIn.setOnClickListener {
//            signInWithGoogle()
        }

        btnLogin.setOnClickListener {
            loginViewModel.login(
                etEmail.text.toString(),
                etPassword.text.toString()
            )
        }

        binding.tvForgotPasswordClickable.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        binding.tvRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
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
                        is LoginEffect.NavigateToHome -> {
                            findNavController().navigate(R.id.action_login_to_home_graph)
                        }
                        is LoginEffect.NavigateToWelcome -> {
                            findNavController().navigate(R.id.action_login_to_welcome_screen)
                        }
                        is LoginEffect.ShowToast -> {
                            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_LONG).show()
                        }
                    }

                }
            }
        }
    }

//    private fun signInWithGoogle() {
//        val credentialManager = CredentialManager.create(requireContext())
//
//        val googleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false)
//            .setServerClientId(getString(R.string.default_web_client_id))
//            .build()
//
//        val request = GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//
//        lifecycleScope.launch {
//            try {
//                val result = credentialManager.getCredential(
//                    request = request,
//                    context = requireContext()
//                )
//                when (val credential = result.credential) {
//                    is CustomCredential -> {
//                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//                            val googleIdTokenCredential =
//                                GoogleIdTokenCredential.createFrom(credential.data)
//                            val googleIdToken = googleIdTokenCredential.idToken
//
//                            firebaseAuthWithGoogle(googleIdToken)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("GoogleSignIn", "Error: ${e.message}")
//            }
//        }
//    }
//
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        FirebaseAuth.getInstance().signInWithCredential(credential)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = FirebaseAuth.getInstance().currentUser
//                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
//                        if (tokenTask.isSuccessful) {
//                            val firebaseIdToken = tokenTask.result?.token
//                            Log.d("FIREBASE_ID_TOKEN", "Firebase ID Token: $firebaseIdToken")
//
//                            // kirim Firebase ID Token ke backend via ViewModel
//                            firebaseIdToken?.let {
//                                loginViewModel.signInWithGoogle(it)
//                            }
//                        } else {
//                            Log.e("FirebaseAuth", "Gagal ambil Firebase ID Token", tokenTask.exception)
//                        }
//                    }
//                } else {
//                    Log.e("FirebaseAuth", "signInWithCredential gagal", task.exception)
//                }
//            }
//    }

}