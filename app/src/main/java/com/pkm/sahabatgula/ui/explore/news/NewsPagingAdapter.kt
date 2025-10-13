package com.pkm.sahabatgula.ui.explore.news

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.newsTimeConverter
import com.pkm.sahabatgula.data.remote.model.NewsItem
import com.pkm.sahabatgula.databinding.ComponentNewsBinding

class NewsPagingAdapter : PagingDataAdapter<NewsItem, NewsPagingAdapter.NewsViewHolder>(NEWS_COMPARATOR) {

    companion object {
        val NEWS_COMPARATOR = object : DiffUtil.ItemCallback<NewsItem>() {
            override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean =
                oldItem.guid == newItem.guid

            override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean =
                oldItem == newItem
        }
    }

    inner class NewsViewHolder(private val binding: ComponentNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NewsItem) {
            binding.tvTitleArticle.text = item.title
            binding.tvDateToday.text = newsTimeConverter(item.pubDate)

            val desc = parseDescription(item.description)
            binding.tvSubtitleArticle.text = desc


            val imageUrl = when {
                !item.thumbnail.isNullOrEmpty() -> item.thumbnail.replace("&amp;", "&")
                item.enclosure?.link?.isNotEmpty() == true -> item.enclosure!!.link!!.replace("&amp;", "&")
                else -> extractFirstImageUrl(item.content ?: item.description ?: "")
            }

            Glide.with(binding.imgNews.context)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgNews)


            binding.root.setOnClickListener {
                val context = it.context
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ComponentNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        getItem(position)?.let { item ->
            Log.d("NEWS_PAGING_ADAPTER","DEBUG_NEWS_ITEM: ${item.title}")
            holder.bind(item)
        }
    }

    private fun extractFirstImageUrl(html: String): String {
        val regex = Regex("<img[^>]+src=\\\"([^\\\"]+)\\\"", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        return match?.groups?.get(1)?.value?.replace("&amp;", "&") ?: ""
    }

    fun parseDescription(raw: String): String {
        val withoutImg = raw.replace(Regex("<img[^>]*>"), "").trim()

        val decoded = Html.fromHtml(withoutImg, Html.FROM_HTML_MODE_LEGACY).toString()

        return decoded.trim()
    }


}
