package com.pkm.sahabatgula.ui.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.HorizontalSpaceItemDecoration
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.databinding.FragmentExploreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private var _binding : FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()

    private lateinit var eventAdapter: EventOnExploreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.carouselItems.observe(viewLifecycleOwner) {  resource ->
            when (resource) {
                is Resource.Success -> {
                    setupCarousel(resource.data)
                }
                is Resource.Error -> {
                    // toast
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // loading
                }
            }

        }

        setupExplore()
        observeEventState()
    }
    private fun setupCarousel(carouselItems: List<CarouselItem>?) {
        if(carouselItems.isNullOrEmpty()) return

        val adapter = CarouselAdapter(carouselItems) { item ->
            if(item.targetUrl != null) {
                openTargetUrl(item.targetUrl)
            } else {
                Toast.makeText(context, "Link Kosong", Toast.LENGTH_SHORT).show()
            }
        }
        binding.viewPagerCarousel.adapter = adapter
        TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerCarousel) {
            tab, position ->
        }.attach()
    }

    private fun openTargetUrl(targetUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
        startActivity(intent)
    }

    private fun setupExplore() {
        eventAdapter = EventOnExploreAdapter()
        binding.rvEvents.apply { // <-- Ganti ID RecyclerView
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // atau VERTICAL

            if (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }

            addItemDecoration(HorizontalSpaceItemDecoration(12))
        }


    }

    private fun observeEventState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
//                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
//                            binding.progressBar.visibility = View.GONE
                            // resource.data tidak akan null di state Success
                            eventAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
//                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

