package edu.praktis.org.newtestapp

import android.util.Log

// Singleton object untuk mengelola daftar barang secara lokal
object BarangRepository {
    private val daftarBarangInternal: MutableList<Barang> = mutableListOf()
    private const val TAG = "BarangRepository"

    // Fungsi untuk menambahkan item baru
    fun addItem(barang: Barang) {
        // Tambahkan di awal list agar item terbaru muncul di atas
        daftarBarangInternal.add(0, barang)
        Log.d(TAG, "Item ditambahkan ke repository: ${barang.nama}, Total: ${daftarBarangInternal.size}")
    }

    // Fungsi untuk mendapatkan semua item
    fun getAllItems(): List<Barang> {
        Log.d(TAG, "Mengambil semua item dari repository: ${daftarBarangInternal.size} item.")
        return ArrayList(daftarBarangInternal) // Kembalikan salinan agar list internal tidak dimodifikasi langsung
    }

    // Fungsi untuk mendapatkan item terbaru (misalnya untuk "Recent Items")
    fun getRecentItems(count: Int): List<Barang> {
        val recentCount = if (count > daftarBarangInternal.size) daftarBarangInternal.size else count
        Log.d(TAG, "Mengambil $recentCount item terbaru dari repository.")
        return ArrayList(daftarBarangInternal.take(recentCount))
    }

    // Fungsi untuk memuat data dummy awal jika repository kosong
    fun loadDummyItemsIfNeeded() {
        if (daftarBarangInternal.isEmpty()) {
            Log.d(TAG, "Repository kosong, memuat data barang dummy...")
            val items = listOf(
                Barang(id = "DUMMY001", nama = "Nike Dunk Low Retro SE", size = "43", harga = 1500000.0, stok = 10, imageUrl = null, status = Status_Barang.Akan_Datang),
                Barang(id = "DUMMY002", nama = "Nike Dunk Low Unlocked", size = "43", harga = 2500000.0, stok = 5, imageUrl = null, status = Status_Barang.Habis),
                Barang(id = "DUMMY003", nama = "Nike Air Force 1 Mid", size = "40", harga = 2000000.0, stok = 15, imageUrl = null, status = Status_Barang.Akan_Datang)
            )
            daftarBarangInternal.addAll(items)
            Log.d(TAG, "Data barang dummy dimuat ke repository: ${daftarBarangInternal.size} item.")
        }
    }

    // Fungsi untuk membersihkan semua item (opsional, untuk reset)
    fun clearAllItems() {
        daftarBarangInternal.clear()
        Log.d(TAG, "Semua item dihapus dari repository.")
    }

    // --- FUNGSI BARU UNTUK HAPUS DAN UPDATE ---

    /**
     * Menghapus item dari repository berdasarkan ID-nya.
     * @param itemId ID dari barang yang akan dihapus.
     * @return true jika item berhasil dihapus, false jika item tidak ditemukan.
     */
    fun removeItem(itemId: String): Boolean {
        val iterator = daftarBarangInternal.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == itemId) {
                iterator.remove()
                Log.d(TAG, "Item dengan ID '$itemId' berhasil dihapus dari repository.")
                return true
            }
        }
        Log.w(TAG, "Item dengan ID '$itemId' tidak ditemukan untuk dihapus.")
        return false
    }

    /**
     * Memperbarui item yang sudah ada di repository.
     * Item dicocokkan berdasarkan ID.
     * @param updatedBarang Objek Barang dengan data yang sudah diperbarui.
     * @return true jika item berhasil diupdate, false jika item dengan ID tersebut tidak ditemukan.
     */
    fun updateItem(updatedBarang: Barang): Boolean {
        val index = daftarBarangInternal.indexOfFirst { it.id == updatedBarang.id }
        if (index != -1) {
            daftarBarangInternal[index] = updatedBarang
            Log.d(TAG, "Item dengan ID '${updatedBarang.id}' berhasil diupdate di repository.")
            return true
        }
        Log.w(TAG, "Item dengan ID '${updatedBarang.id}' tidak ditemukan untuk diupdate.")
        return false
    }

    /**
     * Mendapatkan satu item berdasarkan ID-nya.
     * Berguna untuk mode edit, untuk mengambil data item yang akan diedit.
     * @param itemId ID dari barang yang dicari.
     * @return Objek Barang jika ditemukan, atau null jika tidak.
     */
    fun getItemById(itemId: String): Barang? {
        return daftarBarangInternal.find { it.id == itemId }
    }
}
