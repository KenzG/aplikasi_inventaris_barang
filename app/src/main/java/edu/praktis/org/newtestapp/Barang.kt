package edu.praktis.org.newtestapp

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

enum class Status_Barang {
    Tersedia,
    Habis,
    Akan_Datang
}

@Parcelize
data class Barang(
    @get:Exclude var id: String = "",
    var nama: String = "",
    var size: String = "", // Field size ditambahkan kembali
    var harga: Double = 0.0,
    var stok: Int = 0,
    var imageUrl: String? = null,
    var status: Status_Barang = Status_Barang.Tersedia
) : Parcelable {
    // Konstruktor tanpa argumen diperlukan untuk deserialisasi Firestore
    // dan juga baik untuk ada jika Anda menggunakan library tertentu.
    constructor() : this("", "", "", 0.0, 0, null, Status_Barang.Tersedia)
}