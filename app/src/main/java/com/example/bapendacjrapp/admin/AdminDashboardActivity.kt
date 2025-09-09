package com.example.bapendacjrapp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = Firebase.auth

        val cardManageNews = findViewById<CardView>(R.id.cardManageNews)
        val cardManageArticles = findViewById<CardView>(R.id.cardManageArticles)
        val cardManageAnnouncements = findViewById<CardView>(R.id.cardManageAnnouncements)
        val cardManageServices = findViewById<CardView>(R.id.cardManageServices)
        val cardManageProfile = findViewById<CardView>(R.id.cardManageProfile)
        val cardEditAdminProfile = findViewById<CardView>(R.id.cardEditAdminProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        cardManageNews.setOnClickListener {
            val intent = Intent(this, AdminManageNewsActivity::class.java)
            startActivity(intent)
        }

        cardManageArticles.setOnClickListener {
            val intent = Intent(this, AdminManageArticlesActivity::class.java)
            startActivity(intent)
        }

        cardManageAnnouncements.setOnClickListener {
            val intent = Intent(this, AdminManageAnnouncementsActivity::class.java) // Diubah ke AdminManageAnnouncementsActivity
            startActivity(intent)
        }

        cardManageServices.setOnClickListener {
            val intent = Intent(this, AdminManageServicesActivity::class.java) // Diubah ke AdminManageServicesActivity
            startActivity(intent)
        }

        cardManageProfile.setOnClickListener {
            val intent = Intent(this, AdminProfileInputActivity::class.java)
            startActivity(intent)
        }

        cardEditAdminProfile.setOnClickListener {
            val intent = Intent(this, AdminEditAdminProfileActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil logout.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}