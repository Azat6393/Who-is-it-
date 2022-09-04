package com.woynex.kimbu.feature_settings.presentation.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.woynex.kimbu.R
import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.databinding.ItemNotificationBinding

class NotificationAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NotificationModel, NotificationAdapter.NotificationViewHolder>(DiffCallBack) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        return NotificationViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class NotificationViewHolder(private val _binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(_binding.root) {

        init {
            _binding.root.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onClick(item)
                    }
                }
            }
            _binding.deleteIv.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.delete(item)
                    }
                }
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(item: NotificationModel) {
            _binding.apply {
                titleTv.text = item.title
                textTv.text = item.text
                if (item.is_viewed) {
                    iconIv.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_notifications_24))
                } else {
                    iconIv.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_notifications_active_24))
                }
            }
        }
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<NotificationModel>() {
            override fun areItemsTheSame(
                oldItem: NotificationModel,
                newItem: NotificationModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: NotificationModel,
                newItem: NotificationModel
            ): Boolean {
                return newItem == oldItem
            }
        }
    }

    interface OnItemClickListener {
        fun delete(item: NotificationModel)
        fun onClick(item: NotificationModel)
    }
}