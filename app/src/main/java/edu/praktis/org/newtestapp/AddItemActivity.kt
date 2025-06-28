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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class AddItemActivity : AppCompatActivity() {

    // --- DEKLARASI VIEW ---
    private lateinit var editTextItemName: TextInputEditText
    private lateinit var editTextItemSize: TextInputEditText
    private lateinit var editTextItemPrice: TextInputEditText
    private lateinit var editTextItemQuantity: TextInputEditText
    private lateinit var imageViewUploadPlaceholder: ImageView
    private lateinit var buttonSubmitItem: Button
    private lateinit var switchIncoming: SwitchMaterial // <-- DEKLARASI BARU

    private var imageUri: Uri? = null
    private var isEditMode = false
    private var editBarangId: String? = null
    private var existingBarang: Barang? = null

    private val TAG = "AddItemActivity"

    companion object {
        const val EXTRA_NEW_BARANG = "extra_new_barang"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                imageViewUploadPlaceholder.setImageURI(imageUri)
                Log.d(TAG, "Gambar dipilih: $imageUri")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_item)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- INISIALISASI VIEW ---
        editTextItemName = findViewById(R.id.editTextItemName)
        editTextItemSize = findViewById(R.id.editTextItemSize)
        editTextItemPrice = findViewById(R.id.editTextItemPrice)
        editTextItemQuantity = findViewById(R.id.editTextItemQuantity)
        imageViewUploadPlaceholder = findViewById(R.id.imageViewUploadPlaceholder)
        buttonSubmitItem = findViewById(R.id.buttonSubmitItem)
        switchIncoming = findViewById(R.id.switchIncoming) // <-- INISIALISASI BARU

        // Cek mode Edit atau Tambah
        if (intent.hasExtra(EXTRA_NEW_BARANG)) {
            isEditMode = true
            existingBarang = intent.getParcelableExtra(EXTRA_NEW_BARANG)
            editBarangId = existingBarang?.id
            Log.d(TAG, "Mode Edit untuk barang ID: $editBarangId, Data: $existingBarang")
            populateFieldsForEdit()
            findViewById<TextView>(R.id.textViewAddItemTitle).text = "Edit Item"
            buttonSubmitItem.text = "Update Item"
        } else {
            Log.d(TAG, "Mode Tambah Item Baru.")
            findViewById<TextView>(R.id.textViewAddItemTitle).text = "Add Item"
            buttonSubmitItem.text = "Submit"
        }

        imageViewUploadPlaceholder.setOnClickListener {
            Log.d(TAG, "ImageView untuk upload gambar diklik.")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        buttonSubmitItem.setOnClickListener {
            Log.d(TAG, "Tombol Submit/Update Item diklik.")
            // Logika digabung ke fungsi masing-masing untuk kejelasan
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
            // MODIFIKASI: Atur switch sesuai status barang
            switchIncoming.isChecked = barang.status == Status_Barang.Akan_Datang
            
            if (!barang.imageUrl.isNullOrEmpty()) {
                try {
                    imageUri = Uri.parse(barang.imageUrl)
                    Glide.with(this)
                        .load(barang.imageUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.drawable.broken_image)
                        .into(imageViewUploadPlaceholder)
                } catch (e: Exception) {
                    Log.e(TAG, "Error memuat gambar untuk edit: ${barang.imageUrl}", e)
                    imageViewUploadPlaceholder.setImageResource(R.drawable.broken_image)
                }
            } else {
                imageViewUploadPlaceholder.setImageResource(R.drawable.image)
            }
        }
    }

    private fun submitNewItemToRepository() {
        val nama = editTextItemName.text.toString().trim()
        val size = editTextItemSize.text.toString().trim()
        val hargaString = editTextItemPrice.text.toString().trim()
        val kuantitasString = editTextItemQuantity.text.toString().trim()

        if (!validateInputs(nama, size, hargaString, kuantitasString)) return

        val harga = hargaString.toDouble()
        val stok = kuantitasString.toInt()

        // MODIFIKASI: Tentukan status barang baru
        val status = when {
            switchIncoming.isChecked -> Status_Barang.Akan_Datang
            stok > 0 -> Status_Barang.Tersedia
            else -> Status_Barang.Habis
        }
        Log.d(TAG, "Status barang baru ditentukan sebagai: $status")

        val newBarang = Barang(
            id = UUID.randomUUID().toString(),
            nama = nama,
            size = size,
            harga = harga,
            stok = stok,
            imageUrl = imageUri?.toString(),
            status = status // <-- Masukkan status ke objek
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

        val harga = hargaString.toDouble()
        val stok = kuantitasString.toInt()

        if (editBarangId == null) {
            Log.e(TAG, "Tidak ada ID barang untuk diupdate.")
            Toast.makeText(this, "Error: ID barang tidak ditemukan untuk update.", Toast.LENGTH_SHORT).show()
            return
        }

        // MODIFIKASI: Tentukan status barang yang diedit
        val status = when {
            switchIncoming.isChecked -> Status_Barang.Akan_Datang
            stok > 0 -> Status_Barang.Tersedia
            else -> Status_Barang.Habis
        }
        Log.d(TAG, "Status barang yang diupdate ditentukan sebagai: $status")

        val finalImageUrl = imageUri?.toString() ?: existingBarang?.imageUrl

        val updatedBarang = Barang(
            id = editBarangId!!,
            nama = nama,
            size = size,
            harga = harga,
            stok = stok,
            imageUrl = finalImageUrl,
            status = status // <-- Masukkan status ke objek
        )

        val success = BarangRepository.updateItem(updatedBarang)
        if (success) {
            Log.d(TAG, "Barang berhasil diupdate di repository: $updatedBarang")
            Toast.makeText(this, "${updatedBarang.nama} berhasil diperbarui!", Toast.LENGTH_SHORT).show()

            val resultIntent = Intent()
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
        if (hargaString.isEmpty() || hargaString.toDoubleOrNull() == null || hargaString.toDouble() <= 0) {
            editTextItemPrice.error = "Harga tidak valid"
            requestFocusOnError(editTextItemPrice)
            return false
        }
        if (kuantitasString.isEmpty() || kuantitasString.toIntOrNull() == null || kuantitasString.toInt() < 0) {
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