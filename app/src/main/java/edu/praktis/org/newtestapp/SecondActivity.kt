package edu.praktis.org.newtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu // Import PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View // Import View untuk anchor PopupMenu
import android.widget.ImageView // Import ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class SecondActivity : AppCompatActivity() {

    private lateinit var recyclerViewBarang: RecyclerView
    private lateinit var textViewGreeting: TextView
    private lateinit var cardTotalItems: CardView
    private lateinit var imageViewAvatar: ImageView // Deklarasi untuk ikon user/avatar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var barangAdapter: BarangAdapter
    private val daftarRecentItems: MutableList<Barang> = mutableListOf()

    private val TAG = "SecondActivity"

    private val allItemsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Kembali dari AllItemsActivity. Memuat ulang recent items.")
        loadRecentItemsFromRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        auth = Firebase.auth
        db = Firebase.firestore

        if (auth.currentUser == null) {
            Log.w(TAG, "Pengguna tidak login, mengarahkan ke LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewBarang = findViewById(R.id.recyclerViewRecentItems)
        textViewGreeting = findViewById(R.id.textViewGreeting)
        cardTotalItems = findViewById(R.id.cardTotalItems)
        imageViewAvatar = findViewById(R.id.imageView3) // Inisialisasi ImageView avatar dengan ID yang benar

        loadAndDisplayUserProfile()
        setupRecyclerView()

        if (BarangRepository.getAllItems().isEmpty()){
            BarangRepository.loadDummyItemsIfNeeded()
        }

        cardTotalItems.setOnClickListener {
            Log.d(TAG, "CardView Total Items diklik. Membuka AllItemsActivity.")
            val intent = Intent(this, AllItemsActivity::class.java)
            allItemsLauncher.launch(intent)
        }

        // Menambahkan OnClickListener untuk ikon avatar/user
        imageViewAvatar.setOnClickListener { view ->
            Log.d(TAG, "Ikon Avatar diklik.")
            showUserPopupMenu(view) // Panggil fungsi untuk menampilkan popup menu
        }

        Log.d(TAG, "SecondActivity berhasil dibuat.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SecondActivity onResume, memuat ulang recent items.")
        loadRecentItemsFromRepository()
    }

    private fun showUserPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_user_options, popupMenu.menu) // Anda perlu membuat file menu_user_options.xml

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_profile -> {
                    // TODO: Implementasi navigasi ke halaman View Profile
                    Toast.makeText(this, "View Profile diklik (belum diimplementasikan)", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Menu item View Profile diklik.")
                    true
                }
                R.id.action_log_out -> {
                    Log.d(TAG, "Menu item Log Out diklik.")
                    auth.signOut()
                    BarangRepository.clearAllItems() // Opsional: Bersihkan data lokal saat logout
                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Membersihkan back stack
                    startActivity(intent)
                    finish() // Tutup SecondActivity
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun loadAndDisplayUserProfile() {
        val firebaseUser = auth.currentUser
        firebaseUser?.uid?.let { userId ->
            Log.d(TAG, "Mencoba memuat profil untuk UID: $userId")
            val userDocumentRef = db.collection("users").document(userId)
            userDocumentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject<User>()
                        user?.let { userData ->
                            val displayName = if (userData.nama.isNotEmpty()) {
                                userData.nama
                            } else {
                                userData.email.substringBefore('@').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }
                            textViewGreeting.text = "Hi, $displayName!"
                        }
                    } else {
                        val fallbackName = firebaseUser.email?.substringBefore('@')?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "User"
                        textViewGreeting.text = "Hi, $fallbackName!"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Gagal memuat data profil untuk UID: $userId", exception)
                    // Toast.makeText(this, "Gagal memuat data profil.", Toast.LENGTH_SHORT).show() // Mungkin terlalu mengganggu jika sering gagal
                    val fallbackName = firebaseUser.email?.substringBefore('@')?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "User"
                    textViewGreeting.text = "Hi, $fallbackName!"
                }
        }
    }

    private fun setupRecyclerView() {
        barangAdapter = BarangAdapter(daftarRecentItems)
        recyclerViewBarang.layoutManager = LinearLayoutManager(this)
        recyclerViewBarang.adapter = barangAdapter
        Log.d(TAG, "RecyclerView setup selesai dengan adapter.")
    }

    private fun loadRecentItemsFromRepository() {
        Log.d(TAG, "Memuat recent items dari BarangRepository...")
        val recentItems = BarangRepository.getRecentItems(5)
        daftarRecentItems.clear()
        daftarRecentItems.addAll(recentItems)
        barangAdapter.notifyDataSetChanged()
        Log.d(TAG, "Recent items berhasil dimuat dari repository: ${daftarRecentItems.size} item.")
    }
}
