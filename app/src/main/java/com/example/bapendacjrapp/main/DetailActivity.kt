package com.example.bapendacjrapp.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bapendacjrapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {

    private lateinit var ivDetailImage: ImageView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val title = intent.getStringExtra("title") ?: "Detail"
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        val tvContent = findViewById<TextView>(R.id.tvDetailContent)
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivDetailImage = findViewById(R.id.ivDetailImage)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        tvDetailTitle.text = title
        tvToolbarTitle.text = ""

        val imageResId = intent.getIntExtra("image_res_id", 0)
        val imageUrl = intent.getStringExtra("image_url")

        if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("http")) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_news_image)
                .into(ivDetailImage)
            ivDetailImage.visibility = View.VISIBLE
        } else if (imageResId != 0) {
            ivDetailImage.setImageResource(imageResId)
            ivDetailImage.visibility = View.VISIBLE
        } else {
            ivDetailImage.visibility = View.GONE
        }

        val content = intent.getStringExtra("content")
        if (content != null) {
            tvContent.text = content
        } else {
            tvContent.text = when (title) {
                "Profil Bapenda" -> "Ini adalah halaman profil Badan Pengelolaan dan Pendapatan Daerah Kabupaten Cianjur. Berisi informasi visi, misi, struktur organisasi, dan sejarah Bapenda."
                "Jenis Pajak" -> "Halaman ini menampilkan daftar jenis-jenis pajak daerah yang dikelola oleh Bapenda Cianjur, seperti PBB, BPHTB, Pajak Kendaraan Bermotor, dll. Dilengkapi dengan informasi tarif dan tata cara pembayaran."
                "Berita Terbaru" -> "Dapatkan informasi berita terkini seputar kebijakan pajak, kegiatan Bapenda, dan pengumuman penting lainnya untuk masyarakat Cianjur."
                "Artikel Edukasi" -> "Kumpulan artikel informatif dan edukatif mengenai perpajakan daerah, manfaat pajak, serta tips-tips terkait pengelolaan keuangan pribadi dan bisnis."
                "Edit Profil" -> "Halaman ini memungkinkan pengguna untuk memperbarui informasi profil mereka, seperti nama, alamat, nomor telepon, dan email. Fitur ini memerlukan autentikasi pengguna."
                else -> "Konten untuk halaman ini belum tersedia."
            }
        }

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
    }
}