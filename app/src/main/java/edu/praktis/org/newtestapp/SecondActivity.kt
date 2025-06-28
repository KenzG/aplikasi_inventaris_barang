package edu.praktis.org.newtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class SecondActivity : AppCompatActivity() {

    // --- DEKLARASI ---
    private lateinit var recyclerViewBarang: RecyclerView
    private lateinit var textViewGreeting: TextView
    private lateinit var imageViewAvatar: ImageView

    private lateinit var cardTotalItems: CardView
    private lateinit var cardIncomingStock: CardView
    private lateinit var cardOutOfStock: CardView
    private lateinit var tvTotalItemsCount: TextView
    private lateinit var tvIncomingStockCount: TextView
    private lateinit var tvOutOfStockCount: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var barangAdapter: BarangAdapter
    private val daftarRecentItems: MutableList<Barang> = mutableListOf()

    private val TAG = "SecondActivity"

    private val allItemsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Kembali dari AllItemsActivity.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        auth = Firebase.auth
        db = Firebase.firestore

        if (auth.currentUser == null) {
            Log.w(TAG, "Pengguna tidak login, mengarahkan ke LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        // --- INISIALISASI VIEW ---
        recyclerViewBarang = findViewById(R.id.recyclerViewRecentItems)
        textViewGreeting = findViewById(R.id.textViewGreeting)
        imageViewAvatar = findViewById(R.id.imageView3)
        cardTotalItems = findViewById(R.id.cardTotalItems)
        cardIncomingStock = findViewById(R.id.cardIncomingStock)
        cardOutOfStock = findViewById(R.id.cardOutOfStock)
        tvTotalItemsCount = findViewById(R.id.textViewTotalItemsCount)
        tvIncomingStockCount = findViewById(R.id.textViewIncomingStockCount)
        tvOutOfStockCount = findViewById(R.id.textViewOutOfStockCount)

        loadAndDisplayUserProfile()
        setupRecyclerView()

        if (BarangRepository.getAllItems().isEmpty()){
            BarangRepository.loadDummyItemsIfNeeded()
        }

        setupClickListeners()

        Log.d(TAG, "SecondActivity berhasil dibuat.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SecondActivity onResume, memuat ulang data.")
        loadRecentItemsFromRepository()
        updateStockCounts()
    }

    // --- FUNGSI-FUNGSI ---

    private fun updateStockCounts() {
        val allItems = BarangRepository.getAllItems()

        val totalCount = allItems.size
        val incomingCount = allItems.count { it.status == Status_Barang.Akan_Datang }
        val outOfStockCount = allItems.count { it.stok == 0 }

        tvTotalItemsCount.text = totalCount.toString()
        tvIncomingStockCount.text = incomingCount.toString()
        tvOutOfStockCount.text = outOfStockCount.toString()

        Log.d(TAG, "Jumlah stok diperbarui: Total=$totalCount, Incoming=$incomingCount, OutOfStock=$outOfStockCount")
    }

    private fun setupClickListeners() {
        cardTotalItems.setOnClickListener {
            Log.d(TAG, "CardView Total Items diklik. Membuka AllItemsActivity.")
            val intent = Intent(this, AllItemsActivity::class.java)
            allItemsLauncher.launch(intent)
        }

        imageViewAvatar.setOnClickListener { view ->
            Log.d(TAG, "Ikon Avatar diklik.")
            showUserPopupMenu(view)
        }

        cardIncomingStock.setOnClickListener {
            Log.d(TAG, "CardView Incoming Stock diklik.")
            val intent = Intent(this, DetailStokActivity::class.java)
            intent.putExtra("TIPE_STATUS", "AKAN_DATANG")
            startActivity(intent)
        }

        cardOutOfStock.setOnClickListener {
            Log.d(TAG, "CardView Out of Stock diklik.")
            val intent = Intent(this, DetailStokActivity::class.java)
            intent.putExtra("TIPE_STATUS", "HABIS")
            startActivity(intent)
        }
    }

    private fun showUserPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_user_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_profile -> {
                    Toast.makeText(this, "View Profile diklik (belum diimplementasikan)", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Menu item View Profile diklik.")
                    true
                }
                R.id.action_log_out -> {
                    Log.d(TAG, "Menu item Log Out diklik.")
                    auth.signOut()
                    BarangRepository.clearAllItems()
                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
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