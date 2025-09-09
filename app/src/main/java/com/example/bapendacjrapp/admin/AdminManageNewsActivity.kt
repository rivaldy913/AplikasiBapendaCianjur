package com.example.bapendacjrapp.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.main.BeritaPengumumanItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminManageNewsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvAdminNews: RecyclerView
    private lateinit var btnAddNews: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_news)

        db = Firebase.firestore

        rvAdminNews = findViewById(R.id.rvAdminNews)
        btnAddNews = findViewById(R.id.btnAddNews)
        ivBack = findViewById(R.id.ivBack)

        rvAdminNews.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAddNews.setOnClickListener {
            val intent = Intent(this, AdminNewsInputActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadNews()
    }

    private fun loadNews() {
        val newsList = mutableListOf<BeritaPengumumanItem>()
        db.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val date = document.getString("date") ?: ""
                    val category = document.getString("category") ?: "Berita"
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    newsList.add(BeritaPengumumanItem(id, imageUrl, title, date, category, description))
                }
                rvAdminNews.adapter = AdminNewsAdapter(newsList,
                    onEditClick = { newsItem ->
                        val intent = Intent(this, AdminNewsInputActivity::class.java).apply {
                            putExtra("NEWS_ID", newsItem.id)
                            putExtra("NEWS_TITLE", newsItem.title)
                            putExtra("NEWS_DATE", newsItem.date)
                            putExtra("NEWS_DESCRIPTION", newsItem.description)
                            putExtra("NEWS_IMAGE_URL", newsItem.imageUrl)
                            putExtra("NEWS_CATEGORY", newsItem.category) // Pass category as well
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { newsItem ->
                        showDeleteConfirmationDialog(newsItem)
                    }
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting news: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog(newsItem: BeritaPengumumanItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Berita")
            .setMessage("Apakah Anda yakin ingin menghapus berita '${newsItem.title}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteNews(newsItem.id)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteNews(newsId: String) {
        db.collection("news").document(newsId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berita berhasil dihapus.", Toast.LENGTH_SHORT).show()
                loadNews() // Refresh the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting news: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}