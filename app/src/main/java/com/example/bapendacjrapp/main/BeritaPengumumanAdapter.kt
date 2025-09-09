package com.example.bapendacjrapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.bumptech.glide.Glide

class BeritaPengumumanAdapter(
    private val items: List<BeritaPengumumanItem>,
    private val onItemClick: (BeritaPengumumanItem) -> Unit
) : RecyclerView.Adapter<BeritaPengumumanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val dateCategoryView: TextView = view.findViewById(R.id.itemDateCategory)
        val descriptionView: TextView = view.findViewById(R.id.itemDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_berita_pengumuman, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.imageUrl != null && item.imageUrl.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_news_image)
                .into(holder.imageView)
        } else {
            val imageResId = holder.itemView.context.resources.getIdentifier(
                item.imageUrl, "drawable", holder.itemView.context.packageName
            )
            if (imageResId != 0) {
                holder.imageView.setImageResource(imageResId)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_news_image)
            }
        }

        holder.titleView.text = item.title
        holder.dateCategoryView.text = "${item.date} | ${item.category}"
        holder.descriptionView.text = item.description
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}