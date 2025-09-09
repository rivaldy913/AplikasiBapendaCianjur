package com.example.bapendacjrapp.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bapendacjrapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import com.example.bapendacjrapp.supabase
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

class AdminNewsInputActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    
    private val supabaseUrl = "https://wlujtqiswyubhdjqcnav.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndsdWp0cWlzd3l1YmhkanFjbmF2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM0MDkxMzAsImV4cCI6MjA2ODk4NTEzMH0.vuNI880TSDImPENiQNhclfytAlFzRpVhgAHBGRa2D0c"
    private val storageBucketName = "berita"
    private lateinit var etBeritaTitle: EditText
    private lateinit var etBeritaDate: EditText
    private lateinit var etBeritaDescription: EditText
    private lateinit var btnAddBerita: Button
    private lateinit var ivNewsInputBack: ImageView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var progressBar: ProgressBar


    private var newsIdToEdit: String? = null
    private var selectedImageUri: Uri? = null
    private var imageUrlToUpdate: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).into(ivImagePreview)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_news_input)

        db = Firebase.firestore

        etBeritaTitle = findViewById(R.id.etBeritaTitle)
        etBeritaDate = findViewById(R.id.etBeritaDate)
        etBeritaDescription = findViewById(R.id.etBeritaDescription)
        btnAddBerita = findViewById(R.id.btnAddBerita)
        ivNewsInputBack = findViewById(R.id.ivNewsInputBack)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        progressBar = findViewById(R.id.progressBar)

        newsIdToEdit = intent.getStringExtra("NEWS_ID")
        if (newsIdToEdit != null) {
            tvToolbarTitle.text = getString(R.string.edit_berita)
            btnAddBerita.text = getString(R.string.simpan_perubahan)
            etBeritaTitle.setText(intent.getStringExtra("NEWS_TITLE"))
            etBeritaDate.setText(intent.getStringExtra("NEWS_DATE"))
            etBeritaDescription.setText(intent.getStringExtra("NEWS_DESCRIPTION"))
            imageUrlToUpdate = intent.getStringExtra("NEWS_IMAGE_URL")
            if (!imageUrlToUpdate.isNullOrEmpty()) {
                Glide.with(this).load(imageUrlToUpdate).into(ivImagePreview)
            }
        } else {
            tvToolbarTitle.text = getString(R.string.input_berita_baru)
            btnAddBerita.text = getString(R.string.tambah_berita)
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnAddBerita.setOnClickListener {
            val title = etBeritaTitle.text.toString().trim()
            val date = etBeritaDate.text.toString().trim()
            val description = etBeritaDescription.text.toString().trim()
            val category = "Berita"

            if (title.isEmpty() || date.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua bidang Berita.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToSupabase(title, date, description, category)
            } else {
                saveNewsToFirestore(title, date, description, category, imageUrlToUpdate)
            }
        }

        ivNewsInputBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun uploadImageToSupabase(title: String, date: String, description: String, category: String) {
        val fileName = "${UUID.randomUUID()}.jpg"

        progressBar.visibility = View.VISIBLE
        btnAddBerita.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                if (bytes == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminNewsInputActivity, "Gagal membaca gambar.", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnAddBerita.isEnabled = true
                    }
                    return@launch
                }

                // Upload to Supabase Storage using Supabase client
                val bucket = supabase.storage.from(storageBucketName)
                bucket.upload(fileName, bytes)

                val imageUrl = "$supabaseUrl/storage/v1/object/public/$storageBucketName/$fileName"
                runOnUiThread {
                    saveNewsToFirestore(title, date, description, category, imageUrl)
                    progressBar.visibility = View.GONE
                    btnAddBerita.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddBerita.isEnabled = true
                    Toast.makeText(this@AdminNewsInputActivity, "Gagal mengunggah gambar ke Supabase: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveNewsToFirestore(title: String, date: String, description: String, category: String, imageUrl: String?) {
        val beritaData = hashMapOf(
            "title" to title,
            "date" to date,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "timestamp" to Date()
        )

        if (newsIdToEdit != null) {
            db.collection("news").document(newsIdToEdit!!)
                .set(beritaData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Berita berhasil diperbarui!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui berita: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("news")
                .add(beritaData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Berita berhasil ditambahkan dengan ID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menambahkan berita: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}