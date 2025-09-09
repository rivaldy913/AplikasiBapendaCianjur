// LayananItem.kt
package com.example.bapendacjrapp.main

// Tidak ada import khusus yang diperlukan.

data class LayananItem(
    val id: String,
    val iconResId: Int = 0, // Backup untuk resource drawable lokal
    val imageUrl: String? = null, // URL gambar dari Supabase
    val title: String,
    val description: String
)