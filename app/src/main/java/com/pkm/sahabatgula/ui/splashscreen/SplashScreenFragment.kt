package com.pkm.sahabatgula.ui.splashscreen

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.isNetworkAvailable
import com.pkm.sahabatgula.databinding.FragmentSplashScreenBinding
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment
import com.pkm.sahabatgula.ui.state.showNoInternetDialogAndExit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private val splashViewModel: SplashViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        observeDestination()
        observeUiState()
        observeDestination()
        observeUiState()

        if (!isNetworkAvailable(requireContext())) {
            showNoInternetDialogAndExit()
            return
        }
        splashViewModel.checkUserSession()

    }

    private fun observeDestination() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.destination.collect { destination ->
                    when (destination) {
                        SplashDestination.ONBOARDING_FLOW -> {
                            if (findNavController().currentDestination?.id == R.id.splashscreen_fragment) {
                                findNavController().navigate(R.id.action_splash_to_onboarding)
                            }
                        }
                        SplashDestination.AUTH_FLOW -> {
                            if (findNavController().currentDestination?.id == R.id.splashscreen_fragment) {
                                findNavController().navigate(R.id.action_splash_to_auth)
                            }
                        }
                        SplashDestination.WELCOME_FLOW -> {
                            if (findNavController().currentDestination?.id == R.id.splashscreen_fragment) {
                                findNavController().navigate(R.id.action_splash_welcome)
                            }
                        }
                        SplashDestination.HOME_FLOW -> {
                            if (findNavController().currentDestination?.id == R.id.splashscreen_fragment) {
                                findNavController().navigate(R.id.action_splash_to_home)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.uiState.collect { state ->
                    when (state) {
                        is GlobalUiState.Error -> {
                            val dialog = StateDialogFragment.newInstance(state)
                            dialog.dismissListener = {
                                requireActivity().finishAffinity()
                            }
                            dialog.show(parentFragmentManager, "NoInternetDialog")
                        }
                        else -> Unit
                    }
                }
            }
        }
    }





}

