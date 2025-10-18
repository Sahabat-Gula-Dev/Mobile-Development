package com.pkm.sahabatgula.ui.settings.helpcenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.data.remote.model.FaqItem
import com.pkm.sahabatgula.databinding.ComponentHelpCenterBinding // Ganti dengan path Binding-mu

class FaqPagingDataAdapter : PagingDataAdapter<FaqItem, FaqPagingDataAdapter.FaqViewHolder>(FAQ_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ComponentHelpCenterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = getItem(position)
        if (faqItem != null) {
            holder.bind(faqItem)
        }
    }

    inner class FaqViewHolder(private val binding: ComponentHelpCenterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set listener pada layout utama item
            binding.clickableLayout.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { faq ->
                        faq.isExpanded = !faq.isExpanded
                        notifyItemChanged(position)
                    }
                }
            }
        }

        fun bind(faq: FaqItem) {
            binding.apply {
                tvTitle.text = faq.question
                tvDescription.text = faq.answer

                expandableGroup.isVisible = faq.isExpanded
                icArrowRight.isVisible = !faq.isExpanded
                icArrowExpand.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE

            }
        }
    }

    companion object {
        private val FAQ_COMPARATOR = object : DiffUtil.ItemCallback<FaqItem>() {
            override fun areItemsTheSame(oldItem: FaqItem, newItem: FaqItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FaqItem, newItem: FaqItem) =
                oldItem == newItem
        }
    }
}