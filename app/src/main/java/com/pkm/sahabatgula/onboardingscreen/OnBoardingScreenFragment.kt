package com.pkm.sahabatgula.onboardingscreen

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentOnBoardingScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingScreenFragment : Fragment() {

    private var _binding: FragmentOnBoardingScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by viewModels()

    @Inject
    lateinit var sessionManager: com.pkm.sahabatgula.data.local.SessionManager
    @Inject lateinit var apiService: com.pkm.sahabatgula.data.remote.api.ApiService


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            sessionManager.clearSession()
        }

        viewModel.items.observe(viewLifecycleOwner) { onboardingItems ->
            val adapter = OnboardingAdapter(this, onboardingItems)
            binding.viewPagerContent.adapter = adapter

            TabLayoutMediator(binding.tabLayoutDots, binding.viewPagerContent) { tab, _ ->
                tab.setCustomView(R.layout.component_tab_dot)
            }.attach()

            (binding.tabLayoutDots.getChildAt(0) as? ViewGroup)?.let { strip ->
                for (i in 0 until strip.childCount) {
                    val tabView = strip.getChildAt(i)
                    tabView.setPadding(0, -72, 0, 0)
                    tabView.minimumWidth = 0
                }
            }

            fun resizeDot(tab: TabLayout.Tab, selected: Boolean) {
                val dot = tab.customView?.findViewById<View>(R.id.dot) ?: return
                val lp = dot.layoutParams
                if (selected) {
                    lp.width = 24.dp; lp.height = 8.dp
                } else {
                    lp.width = 8.dp; lp.height = 8.dp
                }
                dot.layoutParams = lp
                dot.isSelected = selected
                dot.requestLayout()
            }

            binding.tabLayoutDots.post {
                val selectedPos = binding.tabLayoutDots.selectedTabPosition.coerceAtLeast(0)
                for (i in 0 until binding.tabLayoutDots.tabCount) {
                    binding.tabLayoutDots.getTabAt(i)?.let { resizeDot(it, i == selectedPos) }
                }
            }

            binding.tabLayoutDots.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) = resizeDot(tab, true)
                override fun onTabUnselected(tab: TabLayout.Tab) = resizeDot(tab, false)
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            binding.viewPagerContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val lastIndex = onboardingItems.lastIndex
                    if (position == lastIndex) {
                        binding.buttonNext.text = "Mulai"
                        binding.buttonSkip.visibility = View.GONE
                    } else {
                        binding.buttonNext.text = "Lanjut"
                        binding.buttonSkip.visibility = View.VISIBLE
                    }
                }
            })

            binding.buttonNext.setOnClickListener {
                val currentItem = binding.viewPagerContent.currentItem
                val lastIndex = onboardingItems.lastIndex
                if (currentItem < lastIndex) {
                    binding.viewPagerContent.currentItem = currentItem + 1
                } else {
                    viewModel.completeOnboarding()
                    viewLifecycleOwner.lifecycleScope.launch {
                        handlePostOnboardingNavigation()
                    }
                }
            }

            binding.buttonSkip.setOnClickListener {
                viewModel.completeOnboarding()
                viewLifecycleOwner.lifecycleScope.launch {
                    handlePostOnboardingNavigation()
                }
            }
        }
    }

    private suspend fun handlePostOnboardingNavigation() {
        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_onboarding_to_register)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val profile = sessionManager.getOrFetchProfile(apiService)

            if (profile == null) {
                sessionManager.clearSession()
                findNavController().navigate(R.id.action_onboarding_to_register)
                return@launch
            }
            if (sessionManager.isProfileCompleted()) {
                findNavController().navigate(R.id.action_onboarding_to_home)
            } else {
                findNavController().navigate(R.id.action_onboarding_to_input_data)
            }
        }
    }

    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
