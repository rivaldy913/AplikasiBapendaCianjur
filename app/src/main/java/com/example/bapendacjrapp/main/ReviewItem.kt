package com.example.bapendacjrapp.main

data class ReviewItem(
    val id: String,
    val userId: String, // ID pengguna yang memberikan ulasan
    val userName: String, // NEW: Tambahkan properti ini untuk nama pengguna
    val reviewText: String,
    val timestamp: Long // Menggunakan Long untuk timestamp Firebase
)