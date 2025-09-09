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

class AdminServicesInputActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etServiceTitle: EditText
    private lateinit var etServiceDescription: EditText
    private lateinit var btnAddService: Button
    private lateinit var ivServiceInputBack: ImageView
    private lateinit var tvToolbarTitle: TextView
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var progressBar: ProgressBar

    private var serviceIdToEdit: String? = null
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
        setContentView(R.layout.activity_admin_services_input)

        db = Firebase.firestore

        etServiceTitle = findViewById(R.id.etServiceTitle)
        etServiceDescription = findViewById(R.id.etServiceDescription)
        btnAddService = findViewById(R.id.btnAddService)
        ivServiceInputBack = findViewById(R.id.ivServiceInputBack)
        tvToolbarTitle = findViewById<LinearLayout>(R.id.serviceInputToolbar).findViewById<TextView>(R.id.tvToolbarTitle)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        progressBar = findViewById(R.id.progressBar)


        serviceIdToEdit = intent.getStringExtra("SERVICE_ID")
        if (serviceIdToEdit != null) {
            tvToolbarTitle.text = "Edit Layanan"
            btnAddService.text = "Simpan Perubahan"
            etServiceTitle.setText(intent.getStringExtra("SERVICE_TITLE"))
            etServiceDescription.setText(intent.getStringExtra("SERVICE_DESCRIPTION"))
            imageUrlToUpdate = intent.getStringExtra("SERVICE_IMAGE_URL")
            if (!imageUrlToUpdate.isNullOrEmpty()) {
                Glide.with(this).load(imageUrlToUpdate).into(ivImagePreview)
            }
        } else {
            tvToolbarTitle.text = "Input Layanan Baru"
            btnAddService.text = "Tambah Layanan"
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }


        btnAddService.setOnClickListener {
            val title = etServiceTitle.text.toString().trim()
            val description = etServiceDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua bidang layanan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToSupabase(title, description)
            } else {
                saveServiceToFirestore(title, description, imageUrlToUpdate)
            }
        }

        ivServiceInputBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun uploadImageToSupabase(title: String, description: String) {
        val fileName = "service_${UUID.randomUUID()}.jpg"

        progressBar.visibility = View.VISIBLE
        btnAddService.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                if (bytes == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminServicesInputActivity, "Gagal membaca gambar.", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnAddService.isEnabled = true
                    }
                    return@launch
                }

                val bucket = supabase.storage.from(storageBucketName)
                bucket.upload(fileName, bytes)

                val imageUrl = "https://wlujtqiswyubhdjqcnav.supabase.co/storage/v1/object/public/$storageBucketName/$fileName"
                runOnUiThread {
                    saveServiceToFirestore(title, description, imageUrl)
                    progressBar.visibility = View.GONE
                    btnAddService.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddService.isEnabled = true
                    Toast.makeText(this@AdminServicesInputActivity, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveServiceToFirestore(title: String, description: String, imageUrl: String?) {
        val serviceData = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "timestamp" to Date()
        )

        if (serviceIdToEdit != null) {
            db.collection("services").document(serviceIdToEdit!!)
                .set(serviceData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan berhasil diperbarui!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui layanan: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("services")
                .add(serviceData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Layanan berhasil ditambahkan dengan ID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menambahkan layanan: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}