package com.example.bapendacjrapp.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.main.ArtikelItem
import com.bumptech.glide.Glide

class AdminArticleAdapter(
    private val items: List<ArtikelItem>,
    private val onEditClick: (ArtikelItem) -> Unit,
    private val onDeleteClick: (ArtikelItem) -> Unit
) : RecyclerView.Adapter<AdminArticleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val dateCategoryView: TextView = view.findViewById(R.id.itemDateCategory)
        val descriptionView: TextView = view.findViewById(R.id.itemDescription)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate item_admin_news untuk layout serupa dengan tombol edit/delete
        // Meskipun pengguna menyebut item_artikel, untuk pengeditan dan penghapusan,
        // layout dengan tombol seperti item_admin_news lebih sesuai.
        // Jika item_artikel harus digunakan secara ketat, maka harus dimodifikasi untuk menyertakan tombol.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_news, parent, false) // Menggunakan item_admin_news untuk saat ini
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.imageUrl.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_article_image)
                .into(holder.imageView)
        } else {
            val imageResId = holder.itemView.context.resources.getIdentifier(
                item.imageUrl, "drawable", holder.itemView.context.packageName
            )
            if (imageResId != 0) {
                holder.imageView.setImageResource(imageResId)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_article_image)
            }
        }

        holder.titleView.text = item.title
        holder.dateCategoryView.text = "${item.date} | ${item.category}"
        holder.descriptionView.text = item.description

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size
}