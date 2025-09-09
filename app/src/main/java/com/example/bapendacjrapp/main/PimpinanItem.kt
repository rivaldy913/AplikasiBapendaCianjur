package com.example.bapendacjrapp.main

data class PimpinanItem(
    val id: String,
    val imageResId: Int = 0, // Backup untuk resource drawable lokal
    val imageUrl: String? = null, // URL gambar dari Supabase
    val name: String,
    val position: String
)