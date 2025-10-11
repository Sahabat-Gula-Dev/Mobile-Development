package com.pkm.sahabatgula.ui.auth.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by viewModels<LoginViewModel>()
    private var stateDialog: StateDialogFragment? = null

    @Inject lateinit var sessionManager: com.pkm.sahabatgula.data.local.SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = binding.btnLogin
        val etEmail = binding.editInputEmail
        val etPassword = binding.editInputPassword

        btnLogin.isEnabled = false

        binding.tvRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.tvForgotPasswordClickable.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        fun updateLoginButtonState() {
            val emailNotEmpty = etEmail.text.toString().isNotBlank()
            val passwordNotEmpty = etPassword.text.toString().isNotBlank()
            btnLogin.isEnabled = emailNotEmpty && passwordNotEmpty
        }

        etEmail.addTextChangedListener { updateLoginButtonState() }
        etPassword.addTextChangedListener { updateLoginButtonState() }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.login(email, password)
            }
        }

        binding.compSignInWithGoogle.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        observeLoginState()
        observeEffect()
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewState.Idle -> hideStateDialog()
                        is LoginViewState.Loading -> {
                            val mode = loginViewModel.loginMode.value
                            if (mode == LoginMode.GOOGLE) {
                                showStateDialog(
                                    GlobalUiState.Loading(
                                        message = "Kami sedang memproses akun Google-mu. Mohon tunggu sebentar…"
                                    )
                                )
                            } else {
                                showStateDialog(
                                    GlobalUiState.Loading(
                                        message = "Gluby sedang menganalisis data kamu, tunggu beberapa detik"
                                    )
                                )
                            }
                        }

                        is LoginViewState.Success -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = if (loginViewModel.loginMode.value == LoginMode.GOOGLE)
                                        "Login Google Berhasil"
                                    else
                                        "Login Berhasil",
                                    message = "Selamat datang kembali. Terima kasih sudah bergabung."
                                )
                            )

                            stateDialog?.dismissListener = {
                                val navEffect = loginViewModel.consumePendingNavigation()
                                when (navEffect) {
                                    is LoginEffect.NavigateToHome -> {
                                        findNavController().navigate(R.id.action_login_to_home_graph)
                                    }
                                    is LoginEffect.NavigateToWelcome -> {
                                        findNavController().navigate(R.id.action_login_to_welcome_screen)
                                    }
                                    null -> {}
                                }
                            }
                        }


                        is LoginViewState.Error -> {
                            val errorTitle = if (loginViewModel.loginMode.value == LoginMode.GOOGLE)
                                "Oops, Login Google Gagal"
                            else
                                "Oops, Ada Masalah"
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = errorTitle,
                                    message = state.message ?: "Terjadi kesalahan, coba lagi."
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.effect.collect { effect ->
                    when (effect) {
                        is LoginEffect.NavigateToHome -> {
                            findNavController().navigate(R.id.action_login_to_home_graph)
                        }
                        is LoginEffect.NavigateToWelcome -> {
                            findNavController().navigate(R.id.action_login_to_welcome_screen)
                        }
                    }
                }
            }
        }
    }

    private fun showStateDialog(state: GlobalUiState) {
        if (stateDialog?.dialog?.isShowing == true) stateDialog?.dismiss()
        stateDialog = StateDialogFragment.newInstance(state)
        stateDialog?.show(childFragmentManager, "LoginDialog")
    }

    private fun hideStateDialog() {
        stateDialog?.dismiss()
        stateDialog = null
    }


    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
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
                            firebaseIdToken?.let {
                                loginViewModel.signInWithGoogle(it)
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