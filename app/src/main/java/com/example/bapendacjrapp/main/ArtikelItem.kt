// ArtikelItem.kt
package com.example.bapendacjrapp.main

data class ArtikelItem(
    val id: String,
    val imageUrl: String, // HARUS String
    val title: String,
    val date: String,
    val category: String,
    val description: String
)