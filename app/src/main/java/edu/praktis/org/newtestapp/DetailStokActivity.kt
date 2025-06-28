package edu.praktis.org.newtestapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DetailStokActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_stok)

        val tvJumlahStok: TextView = findViewById(R.id.tv_jumlah_stok)
        val tvStatusStok: TextView = findViewById(R.id.tv_status_stok)
        val rvDaftarBarang: RecyclerView = findViewById(R.id.rv_daftar_barang)
        val btnKembali: ImageButton = findViewById(R.id.btn_kembali)

        btnKembali.setOnClickListener {
            finish() // Kembali ke layar sebelumnya
        }

        // Ambil tipe status dari Intent yang dikirim dari MainActivity
        val tipeStatusString = intent.getStringExtra("TIPE_STATUS")

        // Asumsikan Anda punya daftar semua barang.
        // Untuk sekarang, kita pakai data statis sebagai contoh.
        // Nanti, ini harus diganti dengan data dari database atau ViewModel.
        val semuaBarangList = BarangRepository.getAllItems()

        val daftarYangTampil: List<Barang>
        val warnaLatarResId: Int
        val judulStatus: String

        if (tipeStatusString == "HABIS") {
            judulStatus = "Out of stock"
            // Filter barang yang stoknya 0
            daftarYangTampil = semuaBarangList.filter { it.stok == 0 }
            warnaLatarResId = R.color.red// Definisikan warna ini di res/values/colors.xml
        } else { // Asumsi lainnya adalah "AKAN_DATANG"
            judulStatus = "Incoming stock"
            // Filter barang dengan status AKAN_DATANG
            daftarYangTampil = semuaBarangList.filter { it.status == Status_Barang.Akan_Datang }
            warnaLatarResId = R.color.dark_grey // Definisikan warna ini di res/values/colors.xml
        }

        // Update UI Header
        tvStatusStok.text = judulStatus
        tvJumlahStok.text = daftarYangTampil.size.toString()

        // Setup Adapter untuk RecyclerView
        val adapter = DetailStokAdapter(daftarYangTampil, ContextCompat.getColor(this, warnaLatarResId))
        rvDaftarBarang.adapter = adapter
    }
}