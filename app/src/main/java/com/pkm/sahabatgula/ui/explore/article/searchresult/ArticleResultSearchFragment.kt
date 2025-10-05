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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val viewModel: ResultSearchArticleViewModel by viewModels()
    private val args:ArticleResultSearchFragment by navArgs()
    private lateinit var articleAdapter: ArticlePagingDataAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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