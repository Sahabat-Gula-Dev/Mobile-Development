package com.pkm.sahabatgula.ui.auth.register

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pkm.sahabatgula.BuildConfig
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.Validator
import com.pkm.sahabatgula.databinding.FragmentRegisterBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel by viewModels<RegisterViewModel>()
    private var stateDialog: StateDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullText = getString(R.string.text_checkbox_register)
        val spannable = SpannableString(fullText)
        val termsText = "Syarat & Ketentuan"
        val termsStart = fullText.indexOf(termsText)
        val termsEnd = termsStart + termsText.length
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.register_to_terms_and_condition)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(primaryColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.checkbox.text = spannable
        binding.checkbox.movementMethod = LinkMovementMethod.getInstance()
        binding.checkbox.highlightColor = android.graphics.Color.TRANSPARENT

        val btnRegister = binding.btnRegister
        val compSignInWithGoogle = binding.compSignInWithGoogle

        compSignInWithGoogle.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }


        btnRegister.setOnClickListener {
            val username = binding.inputUsername.editText?.text.toString().trim()
            val email = binding.inputEmail.editText?.text.toString().trim()
            val password = binding.inputPasswordRegister.editText?.text.toString()

            val usernameError = Validator.validateUsername(username)
            val emailError = Validator.validateEmail(email)
            val passwordError = Validator.validatePassword(password)

            binding.inputUsername.error = usernameError
            binding.inputEmail.error = emailError
            binding.inputPasswordRegister.error = passwordError

            if (usernameError == null && emailError == null && passwordError == null) {
                showStateDialog(
                    GlobalUiState.Loading(
                        message = "Gluby sedang menganalisis data kamu, tunggu beberapa detik"
                    )
                )
                registerViewModel.register(username, email, password)
            }
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
        val isNotEmpty = binding.inputUsername.editText?.text.toString().isNotEmpty() && binding.inputEmail.editText?.text.toString().isNotEmpty() && binding.inputPasswordRegister.editText?.text.toString().isNotEmpty()
        binding.btnRegister.isEnabled = isChecked && !isLoading && isNotEmpty
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
                    when (effect) {
                        is RegisterEffect.ShowError -> {
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = "Registrasi Gagal",
                                    message = "Terjadi kendala saat memproses pendaftaran. Periksa kembali data yang kamu masukkan"
                                )
                            )
                        }

                        is RegisterEffect.ShowInfo -> {
                            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                        }

                        is RegisterEffect.ShowSuccess -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Berhasil",
                                    message = effect.message
                                )
                            )
                        }

                        is RegisterEffect.NavigateToOtpVerification -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Berhasil Bergabung",
                                    message = "Gluby telah mengirimkan kode OTP ke email kamu. Cek kotak masuk dan lanjutkan verifikasinya, ya!"
                                )
                            )
                            stateDialog?.dismissListener = {
                                val bundle = bundleOf("email" to effect.email)
                                // Pastikan navigasi dilakukan setelah fragment resumed
                                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                                    findNavController().navigate(R.id.register_to_otp_verification, bundle)
                                }
                            }
                        }

                        is RegisterEffect.NavigateToHome -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Login Google Berhasil",
                                    message = "Selamat datang kembali!"
                                )
                            )
                            stateDialog?.dismissListener = {
                                // stateDialog?.dismiss() // Opsional, listener biasanya dipanggil setelah dismiss
                                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                                    findNavController().navigate(R.id.register_to_home)
                                }
                            }
                        }

                        is RegisterEffect.NavigateToWelcomeScreen -> {
                            stateDialog?.updateState(
                                GlobalUiState.Success(
                                    title = "Login Google Berhasil",
                                    message = "Akun Google-mu berhasil terhubung. Yuk, lengkapi profilmu dulu!"
                                )
                            )
                            stateDialog?.dismissListener = {
                                // stateDialog?.dismiss() // Opsional
                                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                                    findNavController().navigate(R.id.register_to_welcome_screen)
                                }
                            }
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
            } catch (e: GetCredentialCancellationException) {
                if (BuildConfig.DEBUG) {
                    Log.d("GoogleSignIn", "User canceled sign-in")
                }
                stateDialog?.dismiss()
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Unexpected error: ${e.message}", e)
                stateDialog?.updateState(
                    GlobalUiState.Error(
                        title = "Login Google Gagal",
                        message = "Terjadi kendala saat menghubungkan akun Google. Silakan coba lagi."
                    )
                )
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        showStateDialog(
            GlobalUiState.Loading(
                message = "Memverifikasi akunmu, tunggu sebentar..."
            )
        )
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val firebaseIdToken = tokenTask.result?.token
                            firebaseIdToken?.let {
                                registerViewModel.signInWithGoogle(it)
                            }
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.e("FirebaseAuth", "Gagal ambil Firebase ID Token", tokenTask.exception)
                            }
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = "Login Google Gagal",
                                    message = "Kami tidak dapat memverifikasi akun Google-mu. Silakan coba lagi."
                                )
                            )
                        }
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e("FirebaseAuth", "signInWithCredential gagal", task.exception)
                    }
                    stateDialog?.updateState(
                        GlobalUiState.Error(
                            title = "Login Google Gagal",
                            message = "Terjadi kesalahan saat masuk dengan Google. Silakan coba lagi."
                        )
                    )
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

}

