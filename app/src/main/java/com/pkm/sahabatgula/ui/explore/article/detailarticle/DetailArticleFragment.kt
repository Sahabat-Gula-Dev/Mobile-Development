package com.pkm.sahabatgula.ui.explore.article.detailarticle

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
import com.pkm.sahabatgula.core.utils.convertIsoToIndonesianDateArticle
import com.pkm.sahabatgula.databinding.FragmentDetailArticleBinding
import com.pkm.sahabatgula.ui.explore.ArticleOnExploreAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailArticleFragment : Fragment() {

    private var _binding: FragmentDetailArticleBinding? = null
    private val binding get() = _binding!!
    val args: DetailArticleFragmentArgs by navArgs()

    private lateinit var articleAdapter: ArticleOnExploreAdapter
    private val viewModel: DetailArticleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDetailArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val articleItem = args.articleItemFromExplore

        val articleTitle = articleItem?.title
        val articleCover = articleItem?.coverUrl
        val articleContent = articleItem?.content
        val articleDate = convertIsoToIndonesianDateArticle(articleItem?.createdAt)
        val articleAuthor = "Tim Sahabat Gula"


        binding.apply {
            tvTitleArticle.text = articleTitle
            tvArticleDate.text = articleDate
            tvArticleAuthor.text = articleAuthor

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
                $articleContent
                </body>
            </html>
            """.trimIndent()

            binding.tvArticleDesc.settings.javaScriptEnabled = false
            binding.tvArticleDesc.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "utf-8",
                null
            )



        }
        articleAdapter = ArticleOnExploreAdapter{ article ->
            val action = DetailArticleFragmentDirections.actionDetailToDetailArticle(article)
            view.findNavController().navigate(action)
        }


        binding.rvArticles.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
        observeArticleState()

        Glide.with(requireContext())
            .load(articleCover)
            .placeholder(R.drawable.image_placeholder)
            .into(binding.imgArticle)

        viewModel.loadRelatedArticles(articleItem!!.id)

    }

    private fun observeArticleState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.relatedArticles.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {  }
                        is Resource.Success -> {

                            articleAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, "Terjadi kesalahan saat memuat data artikel", Toast.LENGTH_SHORT).show()
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