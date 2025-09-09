package com.example.bapendacjrapp.main

import android.content.Intent // Import Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.MainActivity // Import MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView // Import BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AllNewsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvAllNews: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var bottomNavigationView: BottomNavigationView // Deklarasi BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_news)

        db = Firebase.firestore

        rvAllNews = findViewById(R.id.rvAllNews)
        ivBack = findViewById(R.id.ivBack)
        bottomNavigationView = findViewById(R.id.bottom_navigation) // Inisialisasi BottomNavigationView

        rvAllNews.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Listener untuk Bottom Navigation Bar
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigasi ke HomeActivity dan hapus tumpukan aktivitas di atasnya
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
                    // Navigasi ke EditProfileActivity
                    val intent = Intent(this, EditProfileActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Membuka Halaman Edit Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        loadAllNews()
    }

    private fun loadAllNews() {
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
                rvAllNews.adapter = BeritaPengumumanAdapter(newsList) { item ->
                    val imageResId = resources.getIdentifier(item.imageUrl, "drawable", packageName)
                    val intent = android.content.Intent(this, DetailActivity::class.java).apply {
                        putExtra("title", item.title)
                        putExtra("content", item.description)
                        putExtra("image_res_id", if (imageResId != 0) imageResId else R.drawable.placeholder_news_image)
                    }
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting all news: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}