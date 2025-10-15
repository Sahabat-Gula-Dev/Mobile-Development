package com.pkm.sahabatgula.ui.explore

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.HorizontalSpaceItemDecoration
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.databinding.FragmentExploreBinding
import com.pkm.sahabatgula.ui.explore.news.NewsPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private var _binding : FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()

    private lateinit var eventAdapter: EventOnExploreAdapter
    private lateinit var articleAdapter: ArticleOnExploreAdapter
    private lateinit var newsAdapter: NewsPagingAdapter
    private var loadingDotsJob: Job? = null

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
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                }
            }

        }

        setupExplore()
        observeEventState()

        setupArticle()
        observeArticleState()

        setupNews()
        binding.rvNews.isNestedScrollingEnabled = false

    }

    private fun setupNews() {
        newsAdapter = NewsPagingAdapter()
        binding.rvNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newsPagingFlow.collectLatest { pagingData ->
                    newsAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            newsAdapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                if (isLoading) {
                    binding.layoutLoading.root.visibility = View.VISIBLE
                    binding.rvNews.visibility = View.GONE
                    startLoadingDotsAnimation()
                } else {
                    binding.layoutLoading.root.visibility = View.GONE
                    binding.rvNews.visibility = View.VISIBLE
                    stopLoadingDotsAnimation()
                }
            }
        }

    }

    private fun observeArticleState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.articleState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {  }
                        is Resource.Success -> {
                            articleAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupArticle() {
        articleAdapter = ArticleOnExploreAdapter{ article ->
            val action = ExploreFragmentDirections.actionExploreToDetailArticle(article)
            view?.findNavController()?.navigate(action)
        }
        binding.rvArticles.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupCarousel(carouselItems: List<CarouselItem>?) {
        if(carouselItems.isNullOrEmpty()) return

        val adapter = CarouselAdapter(carouselItems) { item ->

        }
        binding.viewPagerCarousel.adapter = adapter
        TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerCarousel) {
                tab, position ->
        }.attach()
    }

    private fun setupExplore() {

        eventAdapter = EventOnExploreAdapter(
            onItemClick = { event ->
                val action = ExploreFragmentDirections.actionExploreToDetailEvent(event )
                view?.findNavController()?.navigate(action)
            }
        )
        binding.rvEvents.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            if (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }

            addItemDecoration(HorizontalSpaceItemDecoration(12))
        }
        binding.tvSeeAllArticles.setOnClickListener {
            val action = ExploreFragmentDirections.actionExploreToExploreArticle()
            view?.findNavController()?.navigate(action)

        }
        binding.tvSeeAllEvents.setOnClickListener {
            val action = ExploreFragmentDirections.actionExploreToExploreEvent()
            view?.findNavController()?.navigate(action)
        }


    }

    private fun observeEventState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                        }
                        is Resource.Success -> {
                            eventAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun startLoadingDotsAnimation() {
        val baseText = "Tunggu sebentar"
        val gluby = binding.layoutLoading.imgGlubby
        val titleView = binding.layoutLoading.tvTitle
        val messageView = binding.layoutLoading.tvMessage

        titleView.text = baseText
        messageView.text = "Berita sedang dimuat, tunggu sejenak ya "
        gluby.setImageResource(R.drawable.glubby_progress)

        loadingDotsJob?.cancel()
        loadingDotsJob = lifecycleScope.launch {
            var dotCount = 0
            while (isActive) {
                val dots = ".".repeat(dotCount)
                titleView.text = "$baseText$dots"
                dotCount = (dotCount + 1) % 4
                delay(500)
            }
        }
    }

    private fun stopLoadingDotsAnimation() {
        loadingDotsJob?.cancel()
        loadingDotsJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

