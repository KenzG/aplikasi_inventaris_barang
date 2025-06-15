package edu.praktis.org.newtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllItemsActivity : AppCompatActivity() {

    private lateinit var textViewTotalItemCountHeader: TextView
    private lateinit var buttonOpenAddItem: ImageButton
    private lateinit var recyclerViewAllItems: RecyclerView

    private lateinit var barangAdapter: BarangAdapter
    // daftarSemuaBarang sekarang akan diisi dari BarangRepository
    private val daftarSemuaBarang: MutableList<Barang> = mutableListOf()
    private val TAG = "AllItemsActivity"

    // ActivityResultLauncher untuk menangani hasil dari AddItemActivity
    private val addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Kembali dari AddItemActivity dengan hasil OK. Memuat ulang daftar barang.")
            // Tidak perlu mengambil ParcelableExtra lagi, cukup muat ulang dari repository
            loadItemsFromRepository()
            Toast.makeText(this, "Daftar barang diperbarui!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "AddItemActivity selesai tanpa RESULT_OK (resultCode: ${result.resultCode})")
        }
    }

    // Launcher untuk ItemDetailActivity
    // Pastikan ItemDetailActivity.kt dan konstanta RESULT_CODE_ITEM_DELETED/EDITED sudah ada
    val itemDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == ItemDetailActivity.RESULT_CODE_ITEM_DELETED || result.resultCode == ItemDetailActivity.RESULT_CODE_ITEM_EDITED) {
            Log.d(TAG, "Kembali dari ItemDetailActivity dengan perubahan. Memuat ulang daftar barang.")
            loadItemsFromRepository() // Muat ulang seluruh daftar dari repository
            val message = if (result.resultCode == ItemDetailActivity.RESULT_CODE_ITEM_DELETED) "Item berhasil dihapus." else "Item berhasil diperbarui."
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_items) // Merujuk ke activity_all_items_xml_v1

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewTotalItemCountHeader = findViewById(R.id.textViewTotalItemCountHeader)
        buttonOpenAddItem = findViewById(R.id.buttonOpenAddItem)
        recyclerViewAllItems = findViewById(R.id.recyclerViewAllItems)

        setupRecyclerView()
        // Panggil loadDummyItemsIfNeeded sekali saja, idealnya di Application class atau saat pertama kali repository diakses.
        // Untuk contoh ini, kita panggil di sini jika daftar di repository masih kosong.
        if (BarangRepository.getAllItems().isEmpty()){
            BarangRepository.loadDummyItemsIfNeeded()
        }
        // loadItemsFromRepository() akan dipanggil di onResume

        buttonOpenAddItem.setOnClickListener {
            Log.d(TAG, "Tombol Tambah Item diklik. Membuka AddItemActivity.")
            val intent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(intent) // Gunakan launcher untuk mendapatkan hasil
        }

        Log.d(TAG, "AllItemsActivity berhasil dibuat (Mode Lokal dengan Repository).")
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang data setiap kali Activity ini menjadi visible/aktif.
        // Ini akan memastikan daftar selalu terbaru, termasuk setelah kembali dari AddItemActivity
        // atau dari Activity lain seperti SecondActivity, atau ItemDetailActivity.
        Log.d(TAG, "AllItemsActivity onResume, memuat ulang item dari repository.")
        loadItemsFromRepository()
    }

    private fun setupRecyclerView() {
        // daftarSemuaBarang akan di-update oleh loadItemsFromRepository
        barangAdapter = BarangAdapter(daftarSemuaBarang) // Pastikan BarangAdapter ada dan diimpor dengan benar
        recyclerViewAllItems.layoutManager = LinearLayoutManager(this)
        recyclerViewAllItems.adapter = barangAdapter
        Log.d(TAG, "RecyclerView setup selesai dengan adapter.")
    }

    private fun updateTotalItemCount() {
        textViewTotalItemCountHeader.text = daftarSemuaBarang.size.toString()
    }

    private fun loadItemsFromRepository() {
        Log.d(TAG, "Memuat semua item dari BarangRepository...")
        val items = BarangRepository.getAllItems()
        daftarSemuaBarang.clear()
        daftarSemuaBarang.addAll(items)
        // Gunakan notifyItemRangeChanged atau DiffUtil untuk performa yang lebih baik jika memungkinkan
        barangAdapter.notifyDataSetChanged() // Beritahu adapter bahwa data telah berubah
        updateTotalItemCount()
        Log.d(TAG, "Semua item berhasil dimuat dari repository: ${daftarSemuaBarang.size} item.")
    }

    // Fungsi loadDummyItems() tidak lagi diperlukan di sini karena logikanya sudah ada di BarangRepository.loadDummyItemsIfNeeded()
}
