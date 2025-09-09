package com.example.bapendacjrapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.bumptech.glide.Glide

class PimpinanAdapter(
    private val items: List<PimpinanItem>,
    private val onItemClick: (PimpinanItem) -> Unit
) : RecyclerView.Adapter<PimpinanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val nameView: TextView = view.findViewById(R.id.itemName)
        val positionView: TextView = view.findViewById(R.id.itemPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pimpinan_bapenda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Load image from Supabase URL if available, otherwise use local drawable
        if (!item.imageUrl.isNullOrEmpty() && item.imageUrl.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_car_icon)
                .into(holder.imageView)
        } else if (item.imageResId != 0) {
            holder.imageView.setImageResource(item.imageResId)
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_car_icon)
        }

        holder.nameView.text = item.name
        holder.positionView.text = item.position
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}