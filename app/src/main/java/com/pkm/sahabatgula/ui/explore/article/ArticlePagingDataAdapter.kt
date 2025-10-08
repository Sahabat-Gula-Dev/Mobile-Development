package com.pkm.sahabatgula.ui.explore.article

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.core.utils.convertIsoToIndonesianDateArticle
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.databinding.ComponentArticleBinding

class ArticlePagingDataAdapter(
    private val onItemClick: (Article) -> Unit
) : PagingDataAdapter<Article, ArticlePagingDataAdapter.ArticleViewHolder>(ARTICLE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ComponentArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = ArticleViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val article: Article? = getItem(position)
                onItemClick(article!!)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        if (article != null) {
            holder.bind(article)
        }
    }

    class ArticleViewHolder(private val binding: ComponentArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(article.coverUrl)
                    .into(imgArticle)

                tvDateToday.text = convertIsoToIndonesianDateArticle(article.createdAt)
                tvTitleArticle.text = article.title
                tvArticleAuthor.text = "Sahabat Gula"

                val htmlContent = article.content ?: ""
                val regex = Regex("<p[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(htmlContent)

                val firstParagraph = match?.groups?.get(1)?.value
                    ?.replace(Regex("\\s+"), " ")
                    ?.trim() ?: ""


                tvSubtitleArticle.text = firstParagraph
                tvSubtitleArticle.maxLines = 2
                tvSubtitleArticle.ellipsize = TextUtils.TruncateAt.END
                tvArticleAuthor.text = "Sahabat Gula"
            }
        }
    }

    companion object {
        private val ARTICLE_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
        }
    }
}