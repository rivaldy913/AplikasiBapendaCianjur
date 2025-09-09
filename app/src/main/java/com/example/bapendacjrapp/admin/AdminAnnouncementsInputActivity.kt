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

class AdminAnnouncementsInputActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etAnnouncementTitle: EditText
    private lateinit var etAnnouncementDate: EditText
    private lateinit var etAnnouncementDescription: EditText
    private lateinit var btnAddAnnouncement: Button
    private lateinit var ivAnnouncementInputBack: ImageView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var progressBar: ProgressBar

    private var announcementIdToEdit: String? = null
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
        setContentView(R.layout.activity_admin_announcements_input)

        db = Firebase.firestore

        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle)
        etAnnouncementDate = findViewById(R.id.etAnnouncementDate)
        etAnnouncementDescription = findViewById(R.id.etAnnouncementDescription)
        btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement)
        ivAnnouncementInputBack = findViewById(R.id.ivAnnouncementInputBack)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        progressBar = findViewById(R.id.progressBar)

        // Inisialisasi tvToolbarTitle dari LinearLayout toolbar, dengan cast eksplisit
        tvToolbarTitle = findViewById<LinearLayout>(R.id.announcementInputToolbar).findViewById<TextView>(R.id.tvToolbarTitle)

        // Pastikan TextView untuk judul toolbar di activity_admin_announcements_input.xml memiliki ID tvToolbarTitle
        // (Lihat instruksi di bawah untuk modifikasi XML jika belum ada)

        announcementIdToEdit = intent.getStringExtra("ANNOUNCEMENT_ID")
        if (announcementIdToEdit != null) {
            tvToolbarTitle.text = "Edit Pengumuman"
            btnAddAnnouncement.text = "Simpan Perubahan"
            etAnnouncementTitle.setText(intent.getStringExtra("ANNOUNCEMENT_TITLE"))
            etAnnouncementDate.setText(intent.getStringExtra("ANNOUNCEMENT_DATE"))
            etAnnouncementDescription.setText(intent.getStringExtra("ANNOUNCEMENT_DESCRIPTION"))
            imageUrlToUpdate = intent.getStringExtra("ANNOUNCEMENT_IMAGE_URL")
            if (!imageUrlToUpdate.isNullOrEmpty()) {
                Glide.with(this).load(imageUrlToUpdate).into(ivImagePreview)
            }
        } else {
            tvToolbarTitle.text = "Input Pengumuman Baru"
            btnAddAnnouncement.text = "Tambah Pengumuman"
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnAddAnnouncement.setOnClickListener {
            val title = etAnnouncementTitle.text.toString().trim()
            val date = etAnnouncementDate.text.toString().trim()
            val description = etAnnouncementDescription.text.toString().trim()
            val category = "Pengumuman"

            if (title.isEmpty() || date.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua bidang pengumuman.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToSupabase(title, date, category, description)
            } else {
                saveAnnouncementToFirestore(title, date, category, description, imageUrlToUpdate)
            }
        }

        ivAnnouncementInputBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun uploadImageToSupabase(title: String, date: String, category: String, description: String) {
        val fileName = "announcement_${UUID.randomUUID()}.jpg"

        progressBar.visibility = View.VISIBLE
        btnAddAnnouncement.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                if (bytes == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminAnnouncementsInputActivity, "Gagal membaca gambar.", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnAddAnnouncement.isEnabled = true
                    }
                    return@launch
                }

                val bucket = supabase.storage.from(storageBucketName)
                bucket.upload(fileName, bytes)

                val imageUrl = "https://wlujtqiswyubhdjqcnav.supabase.co/storage/v1/object/public/$storageBucketName/$fileName"
                runOnUiThread {
                    saveAnnouncementToFirestore(title, date, category, description, imageUrl)
                    progressBar.visibility = View.GONE
                    btnAddAnnouncement.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddAnnouncement.isEnabled = true
                    Toast.makeText(this@AdminAnnouncementsInputActivity, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveAnnouncementToFirestore(title: String, date: String, category: String, description: String, imageUrl: String?) {
        val announcementData = hashMapOf(
            "title" to title,
            "date" to date,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "timestamp" to Date()
        )

        if (announcementIdToEdit != null) {
            db.collection("announcements").document(announcementIdToEdit!!)
                .set(announcementData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pengumuman berhasil diperbarui!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui pengumuman: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("announcements")
                .add(announcementData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Pengumuman berhasil ditambahkan dengan ID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menambahkan pengumuman: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}