package com.example.bapendacjrapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.bumptech.glide.Glide

class LayananAdapter(
    private val items: List<LayananItem>,
    private val onItemClick: (LayananItem) -> Unit
) : RecyclerView.Adapter<LayananAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.itemIcon)
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val descriptionView: TextView = view.findViewById(R.id.itemDescription) // Deklarasi TextView deskripsi
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Load image from Supabase URL if available, otherwise use local drawable
        if (!item.imageUrl.isNullOrEmpty() && item.imageUrl.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_file_icon)
                .into(holder.iconView)
        } else if (item.iconResId != 0) {
            holder.iconView.setImageResource(item.iconResId)
        } else {
            holder.iconView.setImageResource(R.drawable.placeholder_file_icon)
        }
        
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}