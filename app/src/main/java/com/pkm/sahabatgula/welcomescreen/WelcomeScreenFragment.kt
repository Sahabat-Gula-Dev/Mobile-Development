package com.pkm.sahabatgula.welcomescreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentWelcomeScreenBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

class WelcomeScreenFragment : Fragment() {

    private var _binding: FragmentWelcomeScreenBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sessionManager: com.pkm.sahabatgula.data.local.SessionManager
    @Inject lateinit var apiService: com.pkm.sahabatgula.data.remote.api.ApiService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWelcomeScreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnNext.isEnabled = false
                try {
                    // ambil profil terbaru; kalau offline, getOrFetchProfile bisa balikin cache kamu
                    val profile = sessionManager.getOrFetchProfile(apiService)

                    val goHome = profile != null && sessionManager.isProfileCompleted()
                    if (goHome) {
                        safeNavigate(R.id.action_welcome_screen_to_home_graph)
                    } else {
                        safeNavigate(R.id.action_welcome_screen_to_input_data_graph)
                    }
                } catch (_: Exception) {
                    // kalau gagal fetch profile, anggap belum lengkap ➜ ke input data
                    safeNavigate(R.id.action_welcome_screen_to_input_data_graph)
                } finally {
                    binding.btnNext.isEnabled = true
                }
            }
        }
    }

    private fun safeNavigate(actionId: Int) {
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.welcome_screen_fragment) {
            nav.navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}