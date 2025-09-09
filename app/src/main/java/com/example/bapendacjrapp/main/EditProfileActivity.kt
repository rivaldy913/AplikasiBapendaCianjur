package com.example.bapendacjrapp.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bapendacjrapp.MainActivity
import com.example.bapendacjrapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore // Import firestore ktx

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // Deklarasi FirebaseFirestore
    private lateinit var tvCurrentEmail: TextView
    private lateinit var etEditProfileFullName: EditText // Deklarasi EditText baru untuk nama lengkap
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var ivEditProfileBack: ImageView
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = Firebase.auth
        db = Firebase.firestore // Inisialisasi FirebaseFirestore

        tvCurrentEmail = findViewById(R.id.tvCurrentEmail)
        etEditProfileFullName = findViewById(R.id.etEditProfileFullName) // Inisialisasi EditText nama lengkap
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        ivEditProfileBack = findViewById(R.id.ivEditProfileBack)
        btnLogout = findViewById(R.id.btnLogout)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Tampilkan email pengguna saat ini
        val user = auth.currentUser
        if (user != null) {
            tvCurrentEmail.text = "Email Saat Ini: ${user.email}"
            loadUserProfile(user.uid) // Muat profil pengguna untuk mengisi nama lengkap
        } else {
            tvCurrentEmail.text = "Email Saat Ini: Tidak Ditemukan"
            Toast.makeText(this, "Tidak ada pengguna yang masuk.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ubah listener btnChangePassword untuk juga menyimpan nama lengkap
        btnChangePassword.setOnClickListener {
            // Kita akan mengubah fungsi changePassword untuk juga menangani update profil
            updateProfileAndChangePassword()
        }

        ivEditProfileBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnLogout.setOnClickListener {
            performLogout()
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
                    true // Karena sudah di halaman profil
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fullName = document.getString("fullName")
                    if (fullName != null) {
                        etEditProfileFullName.setText(fullName)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfileAndChangePassword() {
        val newPassword = etNewPassword.text.toString().trim()
        val confirmNewPassword = etConfirmNewPassword.text.toString().trim()
        val newFullName = etEditProfileFullName.text.toString().trim() // Ambil nama lengkap yang baru

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Tidak ada pengguna yang masuk.", Toast.LENGTH_SHORT).show()
            return
        }

        // Perbarui nama lengkap terlebih dahulu
        val userUpdateData = hashMapOf<String, Any>(
            "fullName" to newFullName
            // Anda bisa menambahkan bidang lain di sini jika ingin diperbarui
        )

        db.collection("users").document(user.uid)
            .update(userUpdateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Nama lengkap berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                // Lanjutkan dengan perubahan kata sandi jika ada input
                if (newPassword.isNotEmpty() || confirmNewPassword.isNotEmpty()) {
                    if (newPassword != confirmNewPassword) {
                        Toast.makeText(this, "Kata sandi baru tidak cocok.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener // Jangan lanjutkan jika password tidak cocok
                    }
                    if (newPassword.length < 6) {
                        Toast.makeText(this, "Kata sandi minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener // Jangan lanjutkan jika password terlalu pendek
                    }

                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Kata sandi berhasil diperbarui!", Toast.LENGTH_LONG).show()
                                etNewPassword.text.clear()
                                etConfirmNewPassword.text.clear()
                                finish()
                            } else {
                                if (task.exception?.message?.contains("requires recent authentication") == true) {
                                    Toast.makeText(this, "Sesi login Anda sudah usang. Mohon login ulang untuk mengubah kata sandi.", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this, "Gagal memperbarui kata sandi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                } else {
                    // Jika tidak ada perubahan password, cukup kembali setelah update nama
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Anda telah berhasil logout.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}