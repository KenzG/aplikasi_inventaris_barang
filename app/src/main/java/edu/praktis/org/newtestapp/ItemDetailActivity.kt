package edu.praktis.org.newtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide // Untuk memuat gambar dari URL atau URI

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var textViewDetailTitle: TextView
    private lateinit var buttonEditItem: ImageButton
    private lateinit var imageViewItemPhoto: ImageView
    private lateinit var textViewDetailNamaBarang: TextView
    private lateinit var textViewDetailSizeBarang: TextView
    private lateinit var textViewDetailPriceBarang: TextView
    private lateinit var textViewDetailQuantityBarang: TextView
    private lateinit var buttonDeleteItem: ImageButton

    private var currentBarang: Barang? = null
    private var currentBarangPosition: Int = -1 // Untuk melacak posisi item jika diperlukan untuk update/delete

    private val TAG = "ItemDetailActivity"

    companion object {
        const val EXTRA_BARANG = "extra_barang"
        const val EXTRA_BARANG_POSITION = "extra_barang_position" // Untuk mengirim posisi item
        const val RESULT_CODE_ITEM_DELETED = 101
        const val RESULT_CODE_ITEM_EDITED = 102 // Jika Anda mengimplementasikan edit
        const val EXTRA_EDITED_BARANG = "extra_edited_barang"
    }

    // Launcher untuk EditItemActivity (jika Anda membuatnya)
    private val editItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Barang>(AddItemActivity.EXTRA_NEW_BARANG)?.let { editedBarang -> // Menggunakan key yang sama dengan AddItemActivity
                Log.d(TAG, "Item berhasil diedit: $editedBarang")
                currentBarang = editedBarang // Update barang saat ini
                displayBarangDetails() // Tampilkan ulang detail
                // Kirim hasil kembali ke AllItemsActivity bahwa item telah diedit
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_EDITED_BARANG, editedBarang)
                resultIntent.putExtra(EXTRA_BARANG_POSITION, currentBarangPosition)
                setResult(RESULT_CODE_ITEM_EDITED, resultIntent)
                // Tidak finish() di sini, biarkan pengguna melihat detail yang sudah diupdate
                Toast.makeText(this, "${editedBarang.nama} berhasil diperbarui.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewDetailTitle = findViewById(R.id.textViewDetailTitle)
        buttonEditItem = findViewById(R.id.buttonEditItem)
        imageViewItemPhoto = findViewById(R.id.imageView4)
        textViewDetailNamaBarang = findViewById(R.id.textViewDetailNamaBarang)
        textViewDetailSizeBarang = findViewById(R.id.textViewDetailSizeBarang)
        textViewDetailPriceBarang = findViewById(R.id.textViewDetailPriceBarang)
        textViewDetailQuantityBarang = findViewById(R.id.textViewDetailQuantityBarang)
        buttonDeleteItem = findViewById(R.id.buttonDeleteItem)

        // Ambil data Barang yang dikirim dari AllItemsActivity
        currentBarang = intent.getParcelableExtra(EXTRA_BARANG)
        currentBarangPosition = intent.getIntExtra(EXTRA_BARANG_POSITION, -1)


        if (currentBarang == null) {
            Log.e(TAG, "Tidak ada data Barang yang diterima.")
            Toast.makeText(this, "Gagal memuat detail barang.", Toast.LENGTH_SHORT).show()
            finish() // Tutup activity jika tidak ada data
            return
        }

        displayBarangDetails()

        buttonEditItem.setOnClickListener {
            Log.d(TAG, "Tombol Edit Item diklik.")
            currentBarang?.let { barang ->
                // Buka AddItemActivity dalam mode edit, kirim data barang saat ini
                val intent = Intent(this, AddItemActivity::class.java).apply {
                    // Anda perlu cara untuk memberitahu AddItemActivity bahwa ini mode edit
                    // dan mengirim data barang yang ada.
                    // Untuk saat ini, kita akan asumsikan AddItemActivity bisa menangani ini
                    // atau Anda bisa membuat EditItemActivity terpisah.
                    // Untuk contoh sederhana, kita akan gunakan AddItemActivity dan mengirim data barang.
                    putExtra(AddItemActivity.EXTRA_NEW_BARANG, barang) // Kirim barang yang ada untuk diedit
                    // Tambahkan flag atau extra untuk menandakan mode edit jika perlu
                    // putExtra("IS_EDIT_MODE", true)
                }
                // Jika menggunakan AddItemActivity untuk edit, pastikan AddItemActivity bisa
                // mengisi field dengan data yang ada dan mengembalikan hasil yang sesuai.
                editItemLauncher.launch(intent)
                Toast.makeText(this, "Membuka halaman edit untuk ${barang.nama}", Toast.LENGTH_SHORT).show()
            }
        }

        buttonDeleteItem.setOnClickListener {
            Log.d(TAG, "Tombol Hapus Item diklik.")
            currentBarang?.let { barang ->
                showDeleteConfirmationDialog(barang)
            }
        }
    }

    private fun displayBarangDetails() {
        currentBarang?.let { barang ->
            textViewDetailTitle.text = barang.nama // Atau biarkan "Items" jika lebih suka
            textViewDetailNamaBarang.text = barang.nama
            textViewDetailSizeBarang.text = barang.size.ifEmpty { "-" }
            textViewDetailPriceBarang.text = "Rp. ${"%,.0f".format(barang.harga).replace(',', '.')},-" // Format harga
            textViewDetailQuantityBarang.text = barang.stok.toString()

            if (!barang.imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(barang.imageUrl)
                    .placeholder(R.mipmap.ic_launcher_round) // Placeholder saat memuat
                    .error(R.drawable.broken_image) // Gambar jika gagal memuat
                    .into(imageViewItemPhoto)
            } else {
                imageViewItemPhoto.setImageResource(R.drawable.sepatu3) // Placeholder default
            }
        }
    }

    private fun showDeleteConfirmationDialog(barang: Barang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Item")
            .setMessage("Apakah Anda yakin ingin menghapus item '${barang.nama}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                deleteItem(barang)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteItem(barang: Barang) {
        // Logika untuk menghapus item dari BarangRepository
        val success = BarangRepository.removeItem(barang.id) // Asumsi ada fungsi removeItem di repository
        if (success) {
            Log.d(TAG, "Item '${barang.nama}' berhasil dihapus dari repository.")
            Toast.makeText(this, "'${barang.nama}' berhasil dihapus.", Toast.LENGTH_SHORT).show()

            // Kirim hasil kembali ke AllItemsActivity bahwa item telah dihapus
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_BARANG_POSITION, currentBarangPosition) // Kirim posisi item yang dihapus
            setResult(RESULT_CODE_ITEM_DELETED, resultIntent)
            finish() // Kembali ke AllItemsActivity
        } else {
            Log.w(TAG, "Gagal menghapus item '${barang.nama}' dari repository (mungkin tidak ditemukan).")
            Toast.makeText(this, "Gagal menghapus item.", Toast.LENGTH_SHORT).show()
        }
    }
}
