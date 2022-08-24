package com.woynex.kimbu.feature_search.presentation.adapter

import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.millisToDate
import com.woynex.kimbu.databinding.ItemCallLogBinding
import com.woynex.kimbu.feature_search.data.model.NumberInfo

class CallHistoryAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NumberInfo, CallHistoryAdapter.CallHistoryViewHolder>(
        DiffCallBack
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        return CallHistoryViewHolder(
            ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            if (position > 0) {
                val currentDate = item.date.millisToDate(Constants.dateFormat)
                val previousDate = getItem(position - 1).date.millisToDate(Constants.dateFormat)
                when {
                    previousDate.isNullOrBlank() -> {
                        holder.bind(item, hasHeader = true)

                    }
                    currentDate == previousDate -> {
                        holder.bind(item, hasHeader = false)
                    }
                    else -> {
                        holder.bind(item, hasHeader = true)
                    }
                }
            } else {
                holder.bind(item, hasHeader = true)
            }
        }
    }

    inner class CallHistoryViewHolder(private val _binding: ItemCallLogBinding) :
        RecyclerView.ViewHolder(_binding.root) {

        init {
            _binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onClick(item)
                    }
                }
            }
            _binding.callButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onCallClick(item)
                    }
                }
            }
        }

        fun bind(item: NumberInfo, hasHeader: Boolean) {
            _binding.apply {
                nameTv.text = item.name
                timeTv.text = item.date.millisToDate(Constants.timeFormat)
                dateTv.text = item.date.millisToDate(Constants.dateFormat)
                dateTv.visibility = if (hasHeader) View.VISIBLE else View.GONE
                when (item.type.toInt()) {
                    CallLog.Calls.INCOMING_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_phone_callback_24)
                    }
                    CallLog.Calls.OUTGOING_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_phone_forwarded_24)
                    }
                    CallLog.Calls.MISSED_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_phone_missed_24)
                    }
                    CallLog.Calls.VOICEMAIL_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_settings_phone_24)
                    }
                    CallLog.Calls.REJECTED_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_phone_disabled_24)
                    }
                    CallLog.Calls.BLOCKED_TYPE -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_block_24)
                    }
                    else -> {
                        _binding.callInfoIv.setBackgroundResource(R.drawable.ic_baseline_phone_24)
                    }
                }
            }
        }
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<NumberInfo>() {
            override fun areItemsTheSame(oldItem: NumberInfo, newItem: NumberInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NumberInfo, newItem: NumberInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(numberInfo: NumberInfo)
        fun onCallClick(numberInfo: NumberInfo)
    }
}