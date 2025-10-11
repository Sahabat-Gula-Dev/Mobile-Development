package com.pkm.sahabatgula.ui.explore


import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.core.utils.convertIsoToIndonesianDateArticle
import com.pkm.sahabatgula.core.utils.formatEventDate
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.databinding.ComponentArticleBinding
import kotlin.apply

class ArticleOnExploreAdapter(
    private val onItemClick: (Article) -> Unit
) : ListAdapter<Article, ArticleOnExploreAdapter.ArticleViewHolder>(ARTICLE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ComponentArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = ArticleViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val article = getItem(position)
                onItemClick(article)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class ArticleViewHolder(private val binding: ComponentArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(article.coverUrl)
                    .into(imgArticle)

                tvDateToday.text = formatEventDate(article.createdAt)
                tvTitleArticle.text = article.title

                val htmlContent = article.content ?: ""
                val plainText = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                val lines = plainText.lines().filter { it.isNotBlank() }
                val firstParagraph = lines.take(2).joinToString("\n")


                tvSubtitleArticle.text = firstParagraph
                tvSubtitleArticle.maxLines = 2
                tvSubtitleArticle.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }

    companion object {
        private val ARTICLE_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
        }
    }
}