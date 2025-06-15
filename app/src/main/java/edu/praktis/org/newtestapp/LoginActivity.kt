package edu.praktis.org.newtestapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.util.Log // Ditambahkan untuk logging
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // Menggunakan ktx.auth
import com.google.firebase.ktx.Firebase // Menggunakan ktx.Firebase

class LoginActivity : AppCompatActivity() {

    // Deklarasi view
    private lateinit var editTextUsername: TextInputEditText // Diasumsikan ini untuk email
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: MaterialButton
    private lateinit var tvGoToRegister: TextView
    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivity" // Tag untuk logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Pastikan layout ini benar

        auth = Firebase.auth
        Log.d(TAG, "LoginActivity onCreate. Current user: ${auth.currentUser?.uid}")

        // --- BAGIAN INI DIHAPUS/DIKOMENTARI AGAR SELALU MEMINTA LOGIN ---
        // // Cek jika pengguna sudah login (sesi sebelumnya masih aktif)
        // // Jika sudah login, langsung arahkan ke SecondActivity
        // if (auth.currentUser != null) {
        //     Toast.makeText(this, "Sesi sebelumnya masih aktif. Langsung ke SecondActivity.", Toast.LENGTH_SHORT).show()
        //     navigateToSecondActivity() // Langsung ke SecondActivity jika sudah login
        //     return
        // }
        // --- AKHIR BAGIAN YANG DIHAPUS/DIKOMENTARI ---

        // Inisialisasi view
        // Pastikan ID di activity_login.xml Anda adalah:
        // Email -> editTextUsername
        // Password -> editTextPassword
        // Tombol Login -> buttonLogin
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        buttonLogin.setOnClickListener {
            performLogin()
        }

        tvGoToRegister.setOnClickListener {
            Log.d(TAG, "TextView 'Daftar segera!' diklik.")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            // Anda bisa memilih untuk tidak memanggil finish() di sini jika ingin pengguna
            // bisa kembali ke halaman Login dari halaman Register dengan tombol back.
        }
    }

    private fun performLogin() {
        val email = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty()) {
            editTextUsername.error = getString(R.string.email_empty_error)
            editTextUsername.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextUsername.error = getString(R.string.invalid_email_format_error)
            editTextUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.password_empty_error)
            editTextPassword.requestFocus()
            return
        }

        Log.d(TAG, "Mencoba login dengan email: $email")
        // Tampilkan ProgressBar di sini jika ada
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Sembunyikan ProgressBar di sini
                if (task.isSuccessful) {
                    Log.d(TAG, "Login berhasil untuk email: $email, UID: ${auth.currentUser?.uid}")
                    Toast.makeText(this, getString(R.string.login_successful_message), Toast.LENGTH_SHORT).show()
                    navigateToSecondActivity()
                } else {
                    Log.w(TAG, "Login gagal untuk email: $email", task.exception)
                    Toast.makeText(
                        baseContext,
                        getString(R.string.login_failed_message, task.exception?.localizedMessage ?: getString(R.string.unknown_error)),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }

    private fun navigateToSecondActivity() {
        Log.d(TAG, "Navigasi ke SecondActivity")
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
        finishAffinity() // Selesaikan semua activity sebelumnya dalam task ini
    }
}
