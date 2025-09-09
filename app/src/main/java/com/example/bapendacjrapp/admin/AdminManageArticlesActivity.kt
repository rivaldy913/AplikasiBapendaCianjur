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
import com.example.bapendacjrapp.main.ArtikelItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminManageArticlesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvAdminArticles: RecyclerView
    private lateinit var btnAddArtikel: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_articles)

        db = Firebase.firestore

        rvAdminArticles = findViewById(R.id.rvAdminArticles)
        btnAddArtikel = findViewById(R.id.btnAddArtikel)
        ivBack = findViewById(R.id.ivBack)

        rvAdminArticles.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAddArtikel.setOnClickListener {
            val intent = Intent(this, AdminArticleInputActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadArticles()
    }

    private fun loadArticles() {
        val articleList = mutableListOf<ArtikelItem>()
        db.collection("articles")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val date = document.getString("date") ?: ""
                    val category = document.getString("category") ?: "Artikel"
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    articleList.add(ArtikelItem(id, imageUrl, title, date, category, description))
                }
                rvAdminArticles.adapter = AdminArticleAdapter(articleList,
                    onEditClick = { articleItem ->
                        val intent = Intent(this, AdminArticleInputActivity::class.java).apply {
                            putExtra("ARTICLE_ID", articleItem.id)
                            putExtra("ARTICLE_TITLE", articleItem.title)
                            putExtra("ARTICLE_DATE", articleItem.date)
                            putExtra("ARTICLE_CATEGORY", articleItem.category)
                            putExtra("ARTICLE_DESCRIPTION", articleItem.description)
                            putExtra("ARTICLE_IMAGE_URL", articleItem.imageUrl)
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { articleItem ->
                        showDeleteConfirmationDialog(articleItem)
                    }
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error mendapatkan artikel: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog(articleItem: ArtikelItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Artikel")
            .setMessage("Apakah Anda yakin ingin menghapus artikel '${articleItem.title}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteArticle(articleItem.id)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteArticle(articleId: String) {
        db.collection("articles").document(articleId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Artikel berhasil dihapus.", Toast.LENGTH_SHORT).show()
                loadArticles() // Muat ulang daftar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error menghapus artikel: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}