package com.example.bapendacjrapp.main

import android.content.Intent // Import Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView // Import BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AllArticlesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvAllArticles: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var bottomNavigationView: BottomNavigationView // Deklarasi BottomNavigationView baru

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_articles)

        db = Firebase.firestore

        rvAllArticles = findViewById(R.id.rvAllArticles)
        ivBack = findViewById(R.id.ivBack)
        bottomNavigationView = findViewById(R.id.bottom_navigation) // Inisialisasi BottomNavigationView

        rvAllArticles.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Listener untuk Bottom Navigation Bar
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.navigation_layanan -> {
                    // Arahkan ke halaman Chatbot AI
                    val intent = Intent(this, TanyaBapendaActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, EditProfileActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Membuka Halaman Edit Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        loadAllArticles()
    }

    private fun loadAllArticles() {
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
                rvAllArticles.adapter = ArtikelAdapter(articleList) { item ->
                    val imageResId = resources.getIdentifier(item.imageUrl, "drawable", packageName)
                    val intent = android.content.Intent(this, DetailActivity::class.java).apply {
                        putExtra("title", item.title)
                        putExtra("content", item.description)
                        putExtra("image_res_id", if (imageResId != 0) imageResId else R.drawable.placeholder_article_image)
                    }
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting all articles: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}