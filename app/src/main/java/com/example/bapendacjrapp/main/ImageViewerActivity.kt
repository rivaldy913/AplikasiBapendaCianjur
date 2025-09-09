package com.example.bapendacjrapp.main

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bapendacjrapp.R
import com.bumptech.glide.Glide

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val imageView = findViewById<ImageView>(R.id.fullscreenImageView)
        val ivBack = findViewById<ImageView>(R.id.ivImageViewerBack)

        // Ambil gambar dari Intent (URL atau resource ID)
        val imageUrl = intent.getStringExtra("image_url")
        val imageResId = intent.getIntExtra("image_res_id", 0)

        if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("http")) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_file_icon)
                .into(imageView)
        } else if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            imageView.setImageResource(R.drawable.placeholder_file_icon)
        }

        // Listener tombol kembali
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}