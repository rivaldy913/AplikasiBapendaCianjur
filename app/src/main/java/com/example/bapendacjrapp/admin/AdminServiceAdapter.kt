package com.example.bapendacjrapp.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.main.LayananItem
import com.bumptech.glide.Glide

class AdminServiceAdapter(
    private val items: List<LayananItem>,
    private val onEditClick: (LayananItem) -> Unit,
    private val onDeleteClick: (LayananItem) -> Unit
) : RecyclerView.Adapter<AdminServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage) // Akan digunakan untuk ikon layanan
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val dateCategoryView: TextView = view.findViewById(R.id.itemDateCategory) // Akan digunakan untuk deskripsi singkat
        val descriptionView: TextView = view.findViewById(R.id.itemDescription) // Akan digunakan untuk deskripsi lebih detail
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_news, parent, false) // Menggunakan layout yang sama dengan item_admin_news
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Load image from Supabase URL if available, otherwise use local drawable
        if (!item.imageUrl.isNullOrEmpty() && item.imageUrl.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_news_image)
                .into(holder.imageView)
        } else if (item.iconResId != 0) {
            holder.imageView.setImageResource(item.iconResId)
            holder.imageView.setColorFilter(holder.itemView.context.resources.getColor(R.color.primaryBlue, null))
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_news_image)
        }

        holder.titleView.text = item.title
        holder.dateCategoryView.text = item.description
        holder.descriptionView.text = item.description
        holder.descriptionView.maxLines = 3

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size
}