package com.woynex.kimbu.feature_search.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.woynex.kimbu.databinding.ItemTagBinding
import com.woynex.kimbu.feature_search.domain.model.Tag

class TagsAdapter : ListAdapter<Tag, TagsAdapter.TagsViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        return TagsViewHolder(
            ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class TagsViewHolder(private val _binding: ItemTagBinding) :
        RecyclerView.ViewHolder(_binding.root) {

        fun bind(item: Tag) {
            _binding.nameTv.text = item.name
        }
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem == newItem
            }
        }
    }
}