plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}

// Blok allprojects untuk repositori
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jcenter.bintray.com") } // Tambahkan ini
    }
}