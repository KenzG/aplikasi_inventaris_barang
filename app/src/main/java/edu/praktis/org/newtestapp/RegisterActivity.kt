package edu.praktis.org.newtestapp

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.util.Patterns // Import Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.praktis.org.newtestapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth // Menggunakan ktx.auth
import com.google.firebase.firestore.firestore // Menggunakan ktx.firestore
import com.google.firebase.Firebase // Menggunakan ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore // Inisialisasi Firestore di sini agar bisa diakses di seluruh kelas
    private val TAG = "RegisterActivity" // Tag untuk logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth // Inisialisasi Firebase Auth

        binding.btnRegister.setOnClickListener {
            performRegistration() // Panggil fungsi yang sudah ada
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Selesaikan activity ini agar tidak kembali saat menekan tombol back
        }
    }

    private fun performRegistration() { // Menggabungkan logika dari onClick listener ke fungsi terpisah
        val email = binding.etEmailRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString().trim()

        // Validasi input
        if (email.isEmpty()) {
            // Gunakan string resource jika memungkinkan untuk error message
            binding.etEmailRegister.error = "Email tidak boleh kosong"
            binding.etEmailRegister.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailRegister.error = "Format email tidak valid"
            binding.etEmailRegister.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.etPasswordRegister.error = "Password tidak boleh kosong"
            binding.etPasswordRegister.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.etPasswordRegister.error = "Password minimal 6 karakter"
            binding.etPasswordRegister.requestFocus()
            return
        }

        // Tampilkan ProgressBar atau loading indicator di sini jika mau (belum diimplementasikan)
        Log.d(TAG, "Memulai proses registrasi untuk email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success untuk email: $email")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { fbUser -> // Menggunakan nama variabel yang lebih deskriptif
                        // Buat objek User untuk disimpan ke Firestore
                        val newUserProfile = User( // Menggunakan nama variabel yang lebih deskriptif
                            uid = fbUser.uid,
                            email = fbUser.email ?: "", // Handle jika email null (seharusnya tidak terjadi untuk email/password auth)
                            nama = "" // Nama bisa diisi nanti atau dari form tambahan saat registrasi
                        )

                        Log.d(TAG, "Mencoba menyimpan profil untuk UID: ${fbUser.uid} dengan data: $newUserProfile")
                        db.collection("users").document(fbUser.uid)
                            .set(newUserProfile)
                            .addOnSuccessListener {
                                Log.d(TAG, "Profil pengguna berhasil disimpan ke Firestore untuk UID: ${fbUser.uid}")
                                Toast.makeText(this, "Registrasi dan data user berhasil disimpan.", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finishAffinity() // Gunakan finishAffinity untuk menutup semua task sebelumnya
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error menyimpan profil pengguna ke Firestore untuk UID: ${fbUser.uid}", e)
                                Toast.makeText(this, "Registrasi berhasil, tapi gagal simpan data user: ${e.message}", Toast.LENGTH_LONG).show()
                                // Pengguna tetap terdaftar di Auth, mungkin arahkan ke login atau beri opsi coba lagi simpan profil
                                startActivity(Intent(this, LoginActivity::class.java))
                                finishAffinity() // Tetap arahkan dan tutup task
                            }
                    } ?: run {
                        // Ini seharusnya jarang terjadi jika task.isSuccessful, tapi baik untuk ada logging
                        Log.w(TAG, "Registrasi Auth berhasil, tetapi firebaseUser (auth.currentUser) adalah null.")
                        Toast.makeText(baseContext, "Registrasi Auth berhasil tetapi user tidak ditemukan untuk penyimpanan profil.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Jika registrasi gagal, tampilkan pesan ke pengguna.
                    Log.w(TAG, "createUserWithEmail:failure untuk email: $email", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Registrasi gagal: ${task.exception?.message}", // Pesan error dari Firebase lebih informatif
                        Toast.LENGTH_LONG,
                    ).show()
                }
                // Sembunyikan ProgressBar di sini jika ada
            }
    }
}
