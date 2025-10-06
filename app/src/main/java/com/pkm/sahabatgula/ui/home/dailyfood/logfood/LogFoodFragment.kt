package com.pkm.sahabatgula.ui.home.dailyfood.logfood

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.databinding.FragmentScanBinding
class LogFoodFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayoutLogFood = binding.tabLayoutLogFood
        val viewPager = binding.viewPager
        viewPager.adapter = LogFoodPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false


        TabLayoutMediator(tabLayoutLogFood, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Scan Makanan"
                1 -> tab.text = "Catat Manual"
            }
        }.attach()


        // 0
        childFragmentManager.setFragmentResultListener(
            "scanResultKey",
            viewLifecycleOwner
        ) { _, bundle ->
            Log.d("DEBUG_NAV", "LogFoodFragment: Listener Menerima Hasil!")

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("uri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("uri")
            }

            if (uri != null) {
                val action =
                    LogFoodFragmentDirections.actionAddLogFoodToScanResultFragment(uri.toString())
                findNavController().navigate(action)
            } else {
                // TAMBAHKAN LOG 4 (jika uri null)
                Log.e("DEBUG_NAV", "LogFoodFragment: Listener dipanggil, TAPI URI null!")
            }
        }

        // 1
        childFragmentManager.setFragmentResultListener(
            "manualSearchKey",
            viewLifecycleOwner
        ) { _, bundle ->
            Log.d("DEBUG_NAV", "LogFoodFragment: Listener Search Menerima Hasil!")

            val query = bundle.getString("searchQuery") ?: ""
            val categoryId = bundle.getString("categoryId") ?: ""

            val resultBundle = Bundle().apply {
                putString("searchQuery", query)
                putString("categoryId", categoryId)
            }

            findNavController().navigate(
                com.pkm.sahabatgula.R.id.action_add_log_food_to_result_search_food_fragment,
                resultBundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}