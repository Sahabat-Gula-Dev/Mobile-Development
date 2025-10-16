package com.pkm.sahabatgula.ui.explore.event.detailevent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.formatEventDateTime
import com.pkm.sahabatgula.databinding.FragmentDetailEventBinding
import com.pkm.sahabatgula.ui.explore.EventOnExploreAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DetailEventFragment : Fragment() {

    private var _binding: FragmentDetailEventBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DetailEventFragmentArgs>()
    private val viewModel: DetailEventViewModel by viewModels()
    private lateinit var eventAdapter: EventOnExploreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val eventItem = args.eventItemFromExplore
        val eventTitle = eventItem?.title
        val eventCover = eventItem?.coverUrl
        val eventContent = eventItem?.content
        val eventDate = eventItem?.eventDate
        val eventLocation = eventItem?.location
        val eventLocationDetail = eventItem?.locationDetail
        val eventStart = eventItem?.eventStart
        val eventEnd = eventItem?.eventEnd





        binding.apply {
            tvTitleEvent.text = eventTitle
            tvEventOrganizer.text = "Tim Sahabat Gula"
            val (date, time) = formatEventDateTime(eventDate, eventStart, eventEnd)
            cardEventDate.tvTitleInfo.text = date
            cardEventLocation.tvTitleInfo.text = time

            cardEventLocation.icCalendar.setImageResource(R.drawable.ic_location)
            cardEventLocation.tvSubtitleInfo.text = eventLocation
            cardEventLocation.tvSubtitleInfo.text = eventLocationDetail

            val htmlContent = """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: jakarta-sans_family;
                        font-size: 14px;
                        line-height: 1.6;
                        letter-spacing: 0.02em;
                        text-align: justify;
                        padding: 0;
                        margin: 0;
                    }
                </style>
            </head>
            <body>
                $eventContent
                </body>
            </html>
            """.trimIndent()

            binding.tvEventDesc.settings.javaScriptEnabled = false
            binding.tvEventDesc.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "utf-8",
                null
            )

            Glide.with(requireContext())
                .load(eventCover)
                .placeholder(R.drawable.image_placeholder)
                .into(imgEvent)
        }

        setupRecyclerView(view)
        observeRelatedEvents()

        viewModel.loadRelatedEvents(eventItem!!.id)
    }


    private fun setupRecyclerView(view: View) {

        eventAdapter = EventOnExploreAdapter { event ->
            val action = DetailEventFragmentDirections.actionDetailEventSelf(event)
            view.findNavController().navigate(action)
        }

        binding.rvEvents.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeRelatedEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.relatedEvents.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {  }
                        is Resource.Success -> {
                            eventAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, "Terjadi kesalahan saat memuat data event", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}