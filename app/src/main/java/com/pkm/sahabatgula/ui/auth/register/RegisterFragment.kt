package com.pkm.sahabatgula.ui.auth.register

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
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
import com.pkm.sahabatgula.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = binding.editInputUsername
        val etEmail = binding.editInputEmail
        val etPassword = binding.editInputPasswordRegister
        val btnRegister = binding.btnRegister
        val compSignInWithGoogle = binding.compSignInWithGoogle

        compSignInWithGoogle.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        btnRegister.setOnClickListener {
            registerViewModel.register(
                etUsername.text.toString(),
                etEmail.text.toString(),
                etPassword.text.toString()
            )
        }

        binding.checkbox.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        binding.btnToLogin.setOnClickListener {
            findNavController().navigate(R.id.register_to_login)
        }

        observeState()
        observeEffect()
    }

    private fun updateButtonState() {
        val isChecked = binding.checkbox.isChecked
        val isLoading = registerViewModel.registerViewState.value is RegisterViewState.Loading

        // tombol register hanya bisa di klik jika checkbox di pilih dan tidak sedang loading
        binding.btnRegister.isEnabled = isChecked && !isLoading
    }

    private fun observeState(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                registerViewModel.registerViewState.collect {
                    updateButtonState()
                }
            }
        }
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                registerViewModel.effect.collect { effect ->
                    when(effect) {
                        is RegisterEffect.ShowToast -> {
                            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_LONG).show()
                        }
                        is RegisterEffect.NavigateToOtpVerification -> {
                            val bundle = bundleOf("email" to effect.email)
                            findNavController().navigate(R.id.register_to_otp_verification, bundle)
                        }
                        is RegisterEffect.NavigateToHome -> {
                            findNavController().navigate(R.id.register_to_home)
                        }
                    }
                }
            }
        }
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id)) //  Web Client ID dari google-services.json
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                when (val credential = result.credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential =
                                GoogleIdTokenCredential.createFrom(credential.data)
                            val googleIdToken = googleIdTokenCredential.idToken

                            // login ke Firebase dengan Google ID Token
                            firebaseAuthWithGoogle(googleIdToken)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error: ${e.message}")
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val firebaseIdToken = tokenTask.result?.token
                            Log.d("FIREBASE_ID_TOKEN", "Firebase ID Token: $firebaseIdToken")

                            // kirim Firebase ID Token ke backend via ViewModel
                            firebaseIdToken?.let {
                                registerViewModel.signInWithGoogle(it)
                            }
                        } else {
                            Log.e("FirebaseAuth", "Gagal ambil Firebase ID Token", tokenTask.exception)
                        }
                    }
                } else {
                    Log.e("FirebaseAuth", "signInWithCredential gagal", task.exception)
                }
            }
    }

}

