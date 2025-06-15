package edu.praktis.org.newtestapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
// import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.firestore.ktx.firestore
// import com.google.firebase.ktx.Firebase
// import com.google.firebase.storage.FirebaseStorage
// import com.google.firebase.storage.ktx.storage
import java.util.UUID // Untuk generate ID lokal

class AddItemActivity : AppCompatActivity() {

    private lateinit var editTextItemName: TextInputEditText
    private lateinit var editTextItemSize: TextInputEditText // Diaktifkan kembali
    private lateinit var editTextItemPrice: TextInputEditText
    private lateinit var editTextItemQuantity: TextInputEditText // Ini akan digunakan untuk field 'stok'
    private lateinit var imageViewUploadPlaceholder: ImageView
    private lateinit var buttonSubmitItem: Button

    // private lateinit var db: FirebaseFirestore // Firebase dinonaktifkan
    // private lateinit var storage: FirebaseStorage // Firebase dinonaktifkan
    private var imageUri: Uri? = null

    private val TAG = "AddItemActivity"

    // Companion object untuk konstanta yang bisa diakses dari luar
    companion object {
        const val EXTRA_NEW_BARANG = "extra_new_barang" // Key untuk mengirim/menerima data Barang
        // const val EXTRA_IS_EDIT_MODE = "extra_is_edit_mode" // Key untuk menandakan mode edit (opsional jika menggunakan EXTRA_NEW_BARANG)
        // const val EXTRA_EDIT_BARANG_ID = "extra_edit_barang_id" // Key untuk ID barang yang diedit (opsional jika mengirim seluruh objek)
    }

    private var isEditMode = false
    private var editBarangId: String? = null // Akan diisi dari existingBarang.id
    private var existingBarang: Barang? = null


    // ActivityResultLauncher untuk memilih gambar
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                imageViewUploadPlaceholder.setImageURI(imageUri) // Tampilkan gambar yang dipilih
                Log.d(TAG, "Gambar dipilih: $imageUri")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Pastikan layout yang digunakan adalah activity_add_item.xml yang sudah diupdate (dengan field size)
        setContentView(R.layout.activity_add_item)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Views
        editTextItemName = findViewById(R.id.editTextItemName)
        editTextItemSize = findViewById(R.id.editTextItemSize) // Inisialisasi editTextItemSize
        editTextItemPrice = findViewById(R.id.editTextItemPrice)
        editTextItemQuantity = findViewById(R.id.editTextItemQuantity) // Ini untuk stok
        imageViewUploadPlaceholder = findViewById(R.id.imageViewUploadPlaceholder)
        buttonSubmitItem = findViewById(R.id.buttonSubmitItem)


        if (intent.hasExtra(EXTRA_NEW_BARANG)) {
            isEditMode = true
            existingBarang = intent.getParcelableExtra(EXTRA_NEW_BARANG) // Ambil objek Barang yang akan diedit
            editBarangId = existingBarang?.id // Ambil ID dari objek Barang yang ada
            Log.d(TAG, "Mode Edit untuk barang ID: $editBarangId, Data: $existingBarang")
            populateFieldsForEdit()
            findViewById<TextView>(R.id.textViewAddItemTitle).text = "Edit Item" // Ubah judul halaman
            buttonSubmitItem.text = "Update Item" // Ubah teks tombol
        } else {
            Log.d(TAG, "Mode Tambah Item Baru.")
            findViewById<TextView>(R.id.textViewAddItemTitle).text = "Add Item"
            buttonSubmitItem.text = "Submit"
        }


        imageViewUploadPlaceholder.setOnClickListener {
            Log.d(TAG, "ImageView untuk upload gambar diklik.")
            // Intent untuk memilih gambar dari galeri
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        buttonSubmitItem.setOnClickListener {
            Log.d(TAG, "Tombol Submit/Update Item diklik.")
            if (isEditMode) {
                updateExistingItemInRepository()
            } else {
                submitNewItemToRepository()
            }
        }
        Log.d(TAG, "AddItemActivity berhasil dibuat.")
    }

    private fun populateFieldsForEdit() {
        existingBarang?.let { barang ->
            editTextItemName.setText(barang.nama)
            editTextItemSize.setText(barang.size)
            editTextItemPrice.setText(barang.harga.toString())
            editTextItemQuantity.setText(barang.stok.toString())
            if (!barang.imageUrl.isNullOrEmpty()) {
                try {
                    imageUri = Uri.parse(barang.imageUrl) // Simpan URI gambar yang ada
                    Glide.with(this)
                        .load(barang.imageUrl)
                        .placeholder(R.mipmap.ic_launcher_round) // Placeholder jika ada
                        .error(R.drawable.broken_image) // Gambar jika error (pastikan drawable ini ada)
                        .into(imageViewUploadPlaceholder)
                } catch (e: Exception) {
                    Log.e(TAG, "Error memuat gambar untuk edit: ${barang.imageUrl}", e)
                    imageViewUploadPlaceholder.setImageResource(R.drawable.broken_image) // Tampilkan gambar error
                }
            } else {
                // Jika tidak ada imageUrl, tampilkan placeholder default atau biarkan kosong
                imageViewUploadPlaceholder.setImageResource(R.drawable.image) // Ganti dengan placeholder yang sesuai
            }
        }
    }

    private fun submitNewItemToRepository() {
        val nama = editTextItemName.text.toString().trim()
        val size = editTextItemSize.text.toString().trim()
        val hargaString = editTextItemPrice.text.toString().trim()
        val kuantitasString = editTextItemQuantity.text.toString().trim()

        if (!validateInputs(nama, size, hargaString, kuantitasString)) return

        val harga = hargaString.toDouble() // Sudah divalidasi di validateInputs
        val stok = kuantitasString.toInt()   // Sudah divalidasi di validateInputs

        val newBarang = Barang(
            id = UUID.randomUUID().toString(),
            nama = nama,
            size = size,
            harga = harga,
            stok = stok,
            imageUrl = imageUri?.toString()
        )

        BarangRepository.addItem(newBarang)
        Log.d(TAG, "Barang baru ditambahkan ke repository: $newBarang")
        Toast.makeText(this, "${newBarang.nama} berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateExistingItemInRepository() {
        val nama = editTextItemName.text.toString().trim()
        val size = editTextItemSize.text.toString().trim()
        val hargaString = editTextItemPrice.text.toString().trim()
        val kuantitasString = editTextItemQuantity.text.toString().trim()

        if (!validateInputs(nama, size, hargaString, kuantitasString)) return

        val harga = hargaString.toDouble() // Sudah divalidasi
        val stok = kuantitasString.toInt()   // Sudah divalidasi

        if (editBarangId == null) {
            Log.e(TAG, "Tidak ada ID barang untuk diupdate.")
            Toast.makeText(this, "Error: ID barang tidak ditemukan untuk update.", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika gambar tidak diubah, gunakan imageUrl yang lama dari existingBarang.
        // Jika gambar diubah (imageUri tidak null), gunakan imageUri yang baru.
        val finalImageUrl = imageUri?.toString() ?: existingBarang?.imageUrl

        val updatedBarang = Barang(
            id = editBarangId!!, // Gunakan ID yang sudah ada
            nama = nama,
            size = size,
            harga = harga,
            stok = stok,
            imageUrl = finalImageUrl
        )

        val success = BarangRepository.updateItem(updatedBarang)
        if (success) {
            Log.d(TAG, "Barang berhasil diupdate di repository: $updatedBarang")
            Toast.makeText(this, "${updatedBarang.nama} berhasil diperbarui!", Toast.LENGTH_SHORT).show()

            val resultIntent = Intent()
            // Kirim barang yang sudah diedit kembali agar ItemDetailActivity bisa memperbarui tampilannya jika perlu
            resultIntent.putExtra(EXTRA_NEW_BARANG, updatedBarang)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            Log.e(TAG, "Gagal mengupdate barang di repository untuk ID: $editBarangId")
            Toast.makeText(this, "Gagal memperbarui barang.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(nama: String, size: String, hargaString: String, kuantitasString: String): Boolean {
        if (nama.isEmpty()) {
            editTextItemName.error = "Nama barang tidak boleh kosong"
            requestFocusOnError(editTextItemName)
            return false
        }
        if (size.isEmpty()) {
            editTextItemSize.error = "Size tidak boleh kosong"
            requestFocusOnError(editTextItemSize)
            return false
        }
        if (hargaString.isEmpty()) {
            editTextItemPrice.error = "Harga tidak boleh kosong"
            requestFocusOnError(editTextItemPrice)
            return false
        }
        val harga = hargaString.toDoubleOrNull()
        if (harga == null || harga <= 0) {
            editTextItemPrice.error = "Harga tidak valid"
            requestFocusOnError(editTextItemPrice)
            return false
        }
        if (kuantitasString.isEmpty()) {
            editTextItemQuantity.error = "Kuantitas (stok) tidak boleh kosong"
            requestFocusOnError(editTextItemQuantity)
            return false
        }
        val stok = kuantitasString.toIntOrNull()
        if (stok == null || stok < 0) {
            editTextItemQuantity.error = "Kuantitas (stok) tidak valid"
            requestFocusOnError(editTextItemQuantity)
            return false
        }
        return true
    }

    private fun requestFocusOnError(view: TextInputEditText) {
        view.requestFocus()
    }
}
