package com.woynex.kimbu.feature_search.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.ItemCountryBinding
import com.woynex.kimbu.feature_search.domain.model.CountryInfo


class CountryAdapter(private val listener: OnItemClickListener) :
    ListAdapter<CountryInfo, CountryAdapter.CountryViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
            ItemCountryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class CountryViewHolder(private val _binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(_binding.root) {

        init {
            _binding.root.setOnClickListener {
                val position = absoluteAdapterPosition
                val item = getItem(position)
                if (item != null) {
                    listener.onClick(item)
                }
            }
        }

        fun bind(item: CountryInfo) {
            _binding.apply {
                countryFlag.load(item.flag) {
                    crossfade(true)
                    placeholder(R.drawable.ic_baseline_image_24)
                    decoderFactory(SvgDecoder.Factory())
                    transformations(CircleCropTransformation())
                    build()
                }
                countryCode.text = item.name
                countryCodeNumber.text = item.number
            }
        }
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<CountryInfo>() {
            override fun areItemsTheSame(oldItem: CountryInfo, newItem: CountryInfo): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CountryInfo, newItem: CountryInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(countryInfo: CountryInfo)
    }

}