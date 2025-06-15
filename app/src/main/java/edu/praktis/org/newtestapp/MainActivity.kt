package edu.praktis.org.newtestapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.praktis.org.newtestapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentLoggedInUser: User? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        if (auth.currentUser == null) {
            Log.w(TAG, "Pengguna tidak login, mengarahkan ke LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        loadUserProfile()

        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "Tombol Logout diklik.")
            auth.signOut()
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        binding.btnUpdateName.setOnClickListener {
            val newName = binding.etEditName.text.toString().trim()
            if (newName.isNotEmpty()) {
                Log.d(TAG, "Tombol Update Nama diklik dengan nama: $newName")
                updateUserNameInFirestore(newName)
            } else {
                binding.etEditName.error = "Nama tidak boleh kosong"
                binding.etEditName.requestFocus()
            }
        }
    }

    private fun loadUserProfile() {
        val firebaseUser = auth.currentUser
        firebaseUser?.uid?.let { userId ->
            Log.d(TAG, "Mencoba memuat profil untuk UID: $userId")
            val userDocumentRef = db.collection("users").document(userId)
            userDocumentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d(TAG, "Dokumen profil ditemukan untuk UID: $userId. Data: ${documentSnapshot.data}")
                        currentLoggedInUser = documentSnapshot.toObject<User>()
                        currentLoggedInUser?.let { userData ->
                            binding.tvWelcomeMessage.text = "Selamat Datang, ${userData.nama.ifEmpty { userData.email }}"
                            binding.tvUserEmail.text = "Email: ${userData.email}"
                            binding.tvUserName.text = "Nama: ${userData.nama.ifEmpty { "Belum diatur" }}"
                            binding.etEditName.setText(userData.nama)
                        }
                    } else {
                        Log.w(TAG, "Dokumen profil TIDAK DITEMUKAN untuk UID: $userId. Ini akan menyebabkan masalah saat update.")
                        binding.tvWelcomeMessage.text = "Selamat Datang, ${firebaseUser.email ?: "Pengguna"}!"
                        binding.tvUserEmail.text = "Email: ${firebaseUser.email ?: "Tidak diketahui"}"
                        binding.tvUserName.text = "Nama: Belum diatur (Data Profil Tidak Ada)"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Gagal memuat data profil untuk UID: $userId", exception)
                    Toast.makeText(this, "Gagal memuat data profil: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } ?: Log.w(TAG, "loadUserProfile: firebaseUser atau UID adalah null.")
    }

    private fun updateUserNameInFirestore(newName: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "updateUserNameInFirestore: userId null, tidak bisa update.")
            Toast.makeText(this, "Pengguna tidak login. Tidak dapat update.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Mencoba update nama untuk UID: $userId menjadi '$newName'")
        val userDocumentRef = db.collection("users").document(userId)

        userDocumentRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    Log.d(TAG, "Dokumen ditemukan, melanjutkan update untuk UID: $userId")
                    userDocumentRef.update("nama", newName)
                        .addOnSuccessListener {
                            Log.d(TAG, "Nama berhasil diperbarui di Firestore untuk UID: $userId")
                            Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            loadUserProfile() // Muat ulang profil untuk menampilkan perubahan
                            binding.etEditName.text.clear()

                            // Navigasi ke SecondActivity setelah update berhasil
                            Log.d(TAG, "Navigasi ke SecondActivity.")
                            val intent = Intent(this, SecondActivity::class.java)
                            startActivity(intent)
                            // Anda bisa memilih untuk finish() MainActivity di sini jika tidak ingin pengguna kembali
                            // ke halaman ini dengan tombol back setelah update.
                            // finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal memperbarui nama di Firestore untuk UID: $userId", e)
                            Toast.makeText(this, "Gagal memperbarui nama: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Log.e(TAG, "Dokumen TIDAK DITEMUKAN sebelum mencoba update untuk UID: $userId. Operasi update akan gagal. Periksa proses registrasi.")
                    Toast.makeText(this, "Data profil pengguna tidak ditemukan. Tidak dapat update. Pastikan registrasi berhasil menyimpan data.", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e(TAG, "Gagal mengecek dokumen sebelum update untuk UID: $userId", task.exception)
                Toast.makeText(this, "Error saat memeriksa data profil sebelum update: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
