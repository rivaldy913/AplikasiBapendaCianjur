// BeritaPengumumanItem.kt
package com.example.bapendacjrapp.main

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Tambahkan import ini jika menggunakan plugin parcelize

@Parcelize // Anotasi ini memerlukan plugin Kotlin Parcelize di build.gradle.kts
data class BeritaPengumumanItem(
    val id: String,
    val imageUrl: String, // HARUS String
    val title: String,
    val date: String,
    val category: String,
    val description: String
) : Parcelable