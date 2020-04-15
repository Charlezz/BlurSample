package com.charlezz.blursample.realtime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.charlezz.blursample.R
import com.charlezz.blursample.databinding.ViewImageItemBinding

class LiveBlurAdapter : RecyclerView.Adapter<LiveBlurAdapter.ViewHolder>() {
    private val items = intArrayOf(
        R.drawable.image_01,
        R.drawable.image_02,
        R.drawable.image_03,
        R.drawable.image_04,
        R.drawable.image_05,
        R.drawable.image_06,
        R.drawable.image_07,
        R.drawable.image_08
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: LiveBlurAdapter.ViewHolder, position: Int) {
        Glide.with(holder.binding.image).load(items[position]).into(holder.binding.image)
    }

    inner class ViewHolder(val binding: ViewImageItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}

