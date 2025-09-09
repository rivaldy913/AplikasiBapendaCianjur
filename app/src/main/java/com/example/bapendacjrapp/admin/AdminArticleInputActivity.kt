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
import android.widget.LinearLayout
import com.example.bapendacjrapp.supabase
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

class AdminArticleInputActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etArtikelTitle: EditText
    private lateinit var etArtikelDate: EditText
    private lateinit var etArtikelCategory: EditText
    private lateinit var etArtikelDescription: EditText
    private lateinit var btnAddArtikel: Button
    private lateinit var ivArticleInputBack: ImageView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var progressBar: ProgressBar

    private var articleIdToEdit: String? = null
    private var selectedImageUri: Uri? = null
    private var imageUrlToUpdate: String? = null
    private val storageBucketName = "berita"

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
        setContentView(R.layout.activity_admin_article_input)

        db = Firebase.firestore

        etArtikelTitle = findViewById(R.id.etArtikelTitle)
        etArtikelDate = findViewById(R.id.etArtikelDate)
        etArtikelCategory = findViewById(R.id.etArtikelCategory)
        etArtikelDescription = findViewById(R.id.etArtikelDescription)
        btnAddArtikel = findViewById(R.id.btnAddArtikel)
        ivArticleInputBack = findViewById(R.id.ivArticleInputBack)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        progressBar = findViewById(R.id.progressBar)

        // Perbaikan di baris ini: Tambahkan cast eksplisit
        tvToolbarTitle = findViewById<LinearLayout>(R.id.articleInputToolbar).findViewById<TextView>(R.id.tvToolbarTitle)

        articleIdToEdit = intent.getStringExtra("ARTICLE_ID")
        if (articleIdToEdit != null) {
            tvToolbarTitle.text = "Edit Artikel"
            btnAddArtikel.text = "Simpan Perubahan"
            etArtikelTitle.setText(intent.getStringExtra("ARTICLE_TITLE"))
            etArtikelDate.setText(intent.getStringExtra("ARTICLE_DATE"))
            etArtikelCategory.setText(intent.getStringExtra("ARTICLE_CATEGORY"))
            etArtikelDescription.setText(intent.getStringExtra("ARTICLE_DESCRIPTION"))
            imageUrlToUpdate = intent.getStringExtra("ARTICLE_IMAGE_URL")
            if (!imageUrlToUpdate.isNullOrEmpty()) {
                Glide.with(this).load(imageUrlToUpdate).into(ivImagePreview)
            }
        } else {
            tvToolbarTitle.text = "Input Artikel Baru"
            btnAddArtikel.text = "Tambah Artikel"
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }


        btnAddArtikel.setOnClickListener {
            val title = etArtikelTitle.text.toString().trim()
            val date = etArtikelDate.text.toString().trim()
            val category = etArtikelCategory.text.toString().trim()
            val description = etArtikelDescription.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || category.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua bidang artikel.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToSupabase(title, date, category, description)
            } else {
                saveArticleToFirestore(title, date, category, description, imageUrlToUpdate)
            }
        }

        ivArticleInputBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun uploadImageToSupabase(title: String, date: String, category: String, description: String) {
        val fileName = "article_${UUID.randomUUID()}.jpg"

        progressBar.visibility = View.VISIBLE
        btnAddArtikel.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                if (bytes == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminArticleInputActivity, "Gagal membaca gambar.", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnAddArtikel.isEnabled = true
                    }
                    return@launch
                }

                val bucket = supabase.storage.from(storageBucketName)
                bucket.upload(fileName, bytes)

                val imageUrl = "https://wlujtqiswyubhdjqcnav.supabase.co/storage/v1/object/public/$storageBucketName/$fileName"
                runOnUiThread {
                    saveArticleToFirestore(title, date, category, description, imageUrl)
                    progressBar.visibility = View.GONE
                    btnAddArtikel.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddArtikel.isEnabled = true
                    Toast.makeText(this@AdminArticleInputActivity, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveArticleToFirestore(title: String, date: String, category: String, description: String, imageUrl: String?) {
        val artikelData = hashMapOf(
            "title" to title,
            "date" to date,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "timestamp" to Date()
        )

        if (articleIdToEdit != null) {
            db.collection("articles").document(articleIdToEdit!!)
                .set(artikelData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Artikel berhasil diperbarui!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui artikel: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("articles")
                .add(artikelData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Artikel berhasil ditambahkan dengan ID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menambahkan artikel: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}