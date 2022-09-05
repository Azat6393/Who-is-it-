package com.woynex.kimbu.feature_settings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.millisToDate
import com.woynex.kimbu.databinding.ItemSearchedDateBinding
import com.woynex.kimbu.feature_search.domain.model.SearchedUser

class SearchedDateAdapter(private val listener: OnItemClickListener) :
    ListAdapter<SearchedUser, SearchedDateAdapter.SearchedDateViewHolder>(
        DiffCallBack
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedDateViewHolder {
        return SearchedDateViewHolder(
            ItemSearchedDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchedDateViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class SearchedDateViewHolder(private val _binding: ItemSearchedDateBinding) :
        RecyclerView.ViewHolder(_binding.root) {

        init {
            _binding.root.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onClick(item.id!!)
                    }
                }
            }
        }

        fun bind(item: SearchedUser) {
            _binding.apply {
                dateTv.text = item.date?.millisToDate(Constants.statisticsDateFormat)
                timeTv.text = item.date?.millisToDate(Constants.timeFormat)
            }
        }
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<SearchedUser>() {
            override fun areItemsTheSame(oldItem: SearchedUser, newItem: SearchedUser): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: SearchedUser, newItem: SearchedUser): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(id: String)
    }
}