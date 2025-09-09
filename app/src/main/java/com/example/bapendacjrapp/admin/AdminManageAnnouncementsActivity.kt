package com.example.bapendacjrapp.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.R
import com.example.bapendacjrapp.main.BeritaPengumumanItem // Menggunakan BeritaPengumumanItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminManageAnnouncementsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvAdminAnnouncements: RecyclerView
    private lateinit var btnAddAnnouncement: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_announcements)

        db = Firebase.firestore

        rvAdminAnnouncements = findViewById(R.id.rvAdminAnnouncements)
        btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement)
        ivBack = findViewById(R.id.ivBack)

        rvAdminAnnouncements.layoutManager = LinearLayoutManager(this)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnAddAnnouncement.setOnClickListener {
            val intent = Intent(this, AdminAnnouncementsInputActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        val announcementList = mutableListOf<BeritaPengumumanItem>()
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val date = document.getString("date") ?: ""
                    val category = document.getString("category") ?: "Pengumuman"
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    announcementList.add(BeritaPengumumanItem(id, imageUrl, title, date, category, description))
                }
                rvAdminAnnouncements.adapter = AdminAnnouncementAdapter(announcementList,
                    onEditClick = { announcementItem ->
                        val intent = Intent(this, AdminAnnouncementsInputActivity::class.java).apply {
                            putExtra("ANNOUNCEMENT_ID", announcementItem.id)
                            putExtra("ANNOUNCEMENT_TITLE", announcementItem.title)
                            putExtra("ANNOUNCEMENT_DATE", announcementItem.date)
                            putExtra("ANNOUNCEMENT_DESCRIPTION", announcementItem.description)
                            putExtra("ANNOUNCEMENT_IMAGE_URL", announcementItem.imageUrl)
                            putExtra("ANNOUNCEMENT_CATEGORY", announcementItem.category) // Sertakan kategori juga
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { announcementItem ->
                        showDeleteConfirmationDialog(announcementItem)
                    }
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error mendapatkan pengumuman: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog(announcementItem: BeritaPengumumanItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengumuman")
            .setMessage("Apakah Anda yakin ingin menghapus pengumuman '${announcementItem.title}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteAnnouncement(announcementItem.id)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAnnouncement(announcementId: String) {
        db.collection("announcements").document(announcementId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Pengumuman berhasil dihapus.", Toast.LENGTH_SHORT).show()
                loadAnnouncements() // Muat ulang daftar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error menghapus pengumuman: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}