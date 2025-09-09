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
import java.util.UUID
import android.widget.LinearLayout
import com.example.bapendacjrapp.supabase
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

class AdminProfileInputActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etVisi: EditText
    private lateinit var etMisi: EditText
    private lateinit var etTujuanDanFungsi: EditText
    private lateinit var etPimpinanList: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var ivProfileInputBack: ImageView
    private lateinit var tvToolbarTitle: TextView
    
    // Components for struktur organisasi image
    private lateinit var ivStrukturPreview: ImageView
    private lateinit var btnSelectStruktur: Button
    private var selectedStrukturUri: Uri? = null
    private var strukturImageUrl: String? = null
    
    // Components for pimpinan image
    private lateinit var ivPimpinanPreview: ImageView
    private lateinit var btnSelectPimpinan: Button
    private var selectedPimpinanUri: Uri? = null
    private var pimpinanImageUrl: String? = null
    
    private lateinit var progressBar: ProgressBar
    private val storageBucketName = "berita"
    private var uploadCounter = 0
    private var totalUploads = 0
    
    private val pickStrukturImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedStrukturUri = result.data?.data
            if (selectedStrukturUri != null) {
                Glide.with(this).load(selectedStrukturUri).into(ivStrukturPreview)
            }
        }
    }
    
    private val pickPimpinanImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPimpinanUri = result.data?.data
            if (selectedPimpinanUri != null) {
                Glide.with(this).load(selectedPimpinanUri).into(ivPimpinanPreview)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile_input)

        db = Firebase.firestore

        etVisi = findViewById(R.id.etVisi)
        etMisi = findViewById(R.id.etMisi)
        etTujuanDanFungsi = findViewById(R.id.etTujuanDanFungsi)
        etPimpinanList = findViewById(R.id.etPimpinanList)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        ivProfileInputBack = findViewById(R.id.ivProfileInputBack)
        
        ivStrukturPreview = findViewById(R.id.ivStrukturPreview)
        btnSelectStruktur = findViewById(R.id.btnSelectStruktur)
        ivPimpinanPreview = findViewById(R.id.ivPimpinanPreview)
        btnSelectPimpinan = findViewById(R.id.btnSelectPimpinan)
        progressBar = findViewById(R.id.progressBar)

        tvToolbarTitle = findViewById<LinearLayout>(R.id.profileInputToolbar).findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle.text = "Kelola Profil Bapenda"

        loadBapendaProfile()
        
        btnSelectStruktur.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickStrukturImage.launch(intent)
        }
        
        btnSelectPimpinan.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickPimpinanImage.launch(intent)
        }

        btnSaveProfile.setOnClickListener {
            saveBapendaProfile()
        }

        ivProfileInputBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadBapendaProfile() {
        db.collection("bapenda_profile").document("currentProfile")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etVisi.setText(document.getString("visi"))
                    etMisi.setText(document.getString("misi"))
                    etTujuanDanFungsi.setText(document.getString("tujuanDanFungsi"))
                    etPimpinanList.setText(document.getString("pimpinanList"))
                    
                    strukturImageUrl = document.getString("strukturImageUrl")
                    pimpinanImageUrl = document.getString("pimpinanImageUrl")
                    
                    if (!strukturImageUrl.isNullOrEmpty()) {
                        Glide.with(this@AdminProfileInputActivity).load(strukturImageUrl).into(ivStrukturPreview)
                    }
                    
                    if (!pimpinanImageUrl.isNullOrEmpty()) {
                        Glide.with(this@AdminProfileInputActivity).load(pimpinanImageUrl).into(ivPimpinanPreview)
                    }
                } else {
                    Toast.makeText(this, "Dokumen profil belum ada. Silakan isi.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveBapendaProfile() {
        val visi = etVisi.text.toString().trim()
        val misi = etMisi.text.toString().trim()
        val tujuanDanFungsi = etTujuanDanFungsi.text.toString().trim()
        val pimpinanListString = etPimpinanList.text.toString().trim()

        if (visi.isEmpty() || misi.isEmpty() || tujuanDanFungsi.isEmpty() || pimpinanListString.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua bidang profil.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Count how many images need to be uploaded
        totalUploads = 0
        uploadCounter = 0
        
        if (selectedStrukturUri != null) totalUploads++
        if (selectedPimpinanUri != null) totalUploads++
        
        if (totalUploads > 0) {
            progressBar.visibility = View.VISIBLE
            btnSaveProfile.isEnabled = false
            
            if (selectedStrukturUri != null) {
                uploadStrukturImage(visi, misi, tujuanDanFungsi, pimpinanListString)
            }
            
            if (selectedPimpinanUri != null) {
                uploadPimpinanImage(visi, misi, tujuanDanFungsi, pimpinanListString)
            }
        } else {
            saveProfileToFirestore(visi, misi, tujuanDanFungsi, pimpinanListString, strukturImageUrl, pimpinanImageUrl)
        }
    }

    private fun uploadStrukturImage(visi: String, misi: String, tujuanDanFungsi: String, pimpinanList: String) {
        val fileName = "struktur_${UUID.randomUUID()}.jpg"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedStrukturUri!!)?.readBytes()
                if (bytes != null) {
                    val bucket = supabase.storage.from(storageBucketName)
                    bucket.upload(fileName, bytes)
                    
                    strukturImageUrl = "https://wlujtqiswyubhdjqcnav.supabase.co/storage/v1/object/public/$storageBucketName/$fileName"
                }
                
                runOnUiThread {
                    uploadCounter++
                    if (uploadCounter >= totalUploads) {
                        saveProfileToFirestore(visi, misi, tujuanDanFungsi, pimpinanList, strukturImageUrl, pimpinanImageUrl)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSaveProfile.isEnabled = true
                    Toast.makeText(this@AdminProfileInputActivity, "Gagal upload struktur organisasi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun uploadPimpinanImage(visi: String, misi: String, tujuanDanFungsi: String, pimpinanList: String) {
        val fileName = "pimpinan_${UUID.randomUUID()}.jpg"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = contentResolver.openInputStream(selectedPimpinanUri!!)?.readBytes()
                if (bytes != null) {
                    val bucket = supabase.storage.from(storageBucketName)
                    bucket.upload(fileName, bytes)
                    
                    pimpinanImageUrl = "https://wlujtqiswyubhdjqcnav.supabase.co/storage/v1/object/public/$storageBucketName/$fileName"
                }
                
                runOnUiThread {
                    uploadCounter++
                    if (uploadCounter >= totalUploads) {
                        saveProfileToFirestore(visi, misi, tujuanDanFungsi, pimpinanList, strukturImageUrl, pimpinanImageUrl)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSaveProfile.isEnabled = true
                    Toast.makeText(this@AdminProfileInputActivity, "Gagal upload pimpinan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun saveProfileToFirestore(visi: String, misi: String, tujuanDanFungsi: String, pimpinanList: String, strukturUrl: String?, pimpinanUrl: String?) {
        val profileData = hashMapOf(
            "visi" to visi,
            "misi" to misi,
            "tujuanDanFungsi" to tujuanDanFungsi,
            "pimpinanList" to pimpinanList,
            "strukturImageUrl" to strukturUrl,
            "pimpinanImageUrl" to pimpinanUrl
        )

        db.collection("bapenda_profile").document("currentProfile")
            .set(profileData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Profil Bapenda berhasil diperbarui!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}