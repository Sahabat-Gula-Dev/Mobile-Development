package com.pkm.sahabatgula.ui.explore.article.searchresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentResultSearchBinding
import com.pkm.sahabatgula.ui.explore.article.ArticlePagingDataAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleResultSearchFragment : Fragment() {

    private var _binding: FragmentResultSearchBinding ? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleResultSearchViewModel by viewModels()
    private lateinit var articleAdapter: ArticlePagingDataAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupUI()
        setupRecyclerView()
        observeArticles()
    }

    private fun setupUI() {
        val query = viewModel.query
        val formattedText = getString(R.string.search_result_subtitle, query)
        binding.tvSubtitleResult.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)

    }

    private fun setupRecyclerView() {

        articleAdapter = ArticlePagingDataAdapter { article ->
            // Arahkan ke detail artikel
            val action = ArticleResultSearchFragmentDirections.actionArticleResultSearchToDetailArticle(article)
            findNavController().navigate(action)
        }
        binding.rvSearchResult.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(context)
        }

        articleAdapter.addLoadStateListener { loadStates ->
            val isListEmpty = articleAdapter.itemCount == 0 &&
                    loadStates.refresh is LoadState.NotLoading

            if (isListEmpty) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                binding.layoutEmpty.tvTitle.text = "Tidak Ada Hasil"
                binding.layoutEmpty.tvMessage.text = "Kami tidak menemukan apapun untuk pencarian ini. Coba gunakan kata kunci lain."
                binding.rvSearchResult.visibility = View.GONE
                binding.tvHeaderResult.visibility = View.GONE
                binding.tvSubtitleResult.visibility = View.GONE
                binding.topAppBar.setNavigationIcon(R.drawable.ic_close)
            } else {
                binding.layoutEmpty.root.visibility = View.GONE
                binding.rvSearchResult.visibility = View.VISIBLE
                binding.tvHeaderResult.visibility = View.VISIBLE
                binding.tvSubtitleResult.visibility = View.VISIBLE
                binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
            }

            // Kalau error saat load
            val errorState = loadStates.refresh as? LoadState.Error
            if (errorState != null) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                binding.layoutEmpty.tvMessage.text = "Terjadi kesalahan, coba periksa koneksi internetmu lalu muat ulang halaman"
                binding.rvSearchResult.visibility = View.GONE
                binding.tvHeaderResult.visibility = View.GONE
                binding.tvSubtitleResult.visibility = View.GONE
            }
        }
    }

    private fun observeArticles() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.articles.collectLatest { pagingData ->
                    articleAdapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}