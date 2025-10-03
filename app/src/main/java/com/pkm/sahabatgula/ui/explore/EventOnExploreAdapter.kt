package com.pkm.sahabatgula.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.formatEventDate
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.databinding.ComponentEventBinding

class EventOnExploreAdapter: ListAdapter<Event, EventOnExploreAdapter.EventOnExploreViewHolder>(EVENT_COMPARATOR) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventOnExploreViewHolder {
        val binding = ComponentEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventOnExploreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventOnExploreViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
    class EventOnExploreViewHolder(private val binding: ComponentEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // Load gambar dengan Glide
                Glide.with(itemView.context)
                    .load(event.coverUrl)
                    .placeholder(R.drawable.img_event) // Gambar default saat loading
                    .error(R.drawable.img_event) // Gambar jika terjadi error
                    .into(imgArticle)

                // Set data ke TextViews
                tvDateToday.text = formatEventDate(event.eventDate)
                tvTitleEvent.text = event.title
                tvEventOrganizer.text = event.location

                // API tidak menyediakan subtitle.
                tvSubtitleEvent.text = "Deskripsi acara akan ditampilkan di sini" // Placeholder
                }
        }
    }

    companion object {
        private val EVENT_COMPARATOR = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Event, newItem: Event) =
                oldItem == newItem
        }
    }
}

