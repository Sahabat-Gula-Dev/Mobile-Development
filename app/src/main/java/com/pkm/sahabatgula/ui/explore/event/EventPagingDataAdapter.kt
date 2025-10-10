package com.pkm.sahabatgula.ui.explore.event

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.core.utils.formatEventDate
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.databinding.ComponentEventBinding

class EventPagingDataAdapter(
    private val onItemClick: (Event) -> Unit
) : PagingDataAdapter<Event, EventPagingDataAdapter.EventViewHolder>(EVENT_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ComponentEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = EventViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val event: Event? = getItem(position)
                onItemClick(event!!)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.bind(event)
        }
    }

    class EventViewHolder(private val binding: ComponentEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(event.coverUrl)
                    .into(imgArticle)

                tvDateToday.text = formatEventDate(event.createdAt)
                tvTitleEvent.text = event.title

                val htmlContent = event.content ?: ""
                val regex = Regex("<p[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(htmlContent)

                val firstParagraph = match?.groups?.get(1)?.value
                    ?.replace(Regex("\\s+"), " ")
                    ?.trim() ?: ""


                tvSubtitleEvent.text = firstParagraph
                tvSubtitleEvent.maxLines = 2
                tvSubtitleEvent.ellipsize = TextUtils.TruncateAt.END

            }
        }
    }

    companion object {
        private val EVENT_COMPARATOR = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
        }
    }
}