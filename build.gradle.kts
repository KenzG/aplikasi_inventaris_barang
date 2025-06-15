// build.gradle.kts (Project level)
plugins {
    // Tambahkan plugin Google Services di sini jika belum ada
    id("com.google.gms.google-services") version "4.4.2" apply false // Cek versi terbaru
    // Plugin Android Application dan Kotlin (biasanya sudah ada)
    id("com.android.application") version "8.9.2" apply false // Sesuaikan dengan versi Android Gradle Plugin Anda
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false // Sesuaikan dengan versi Kotlin Anda
}