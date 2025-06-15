package edu.praktis.org.newtestapp // Atau edu.praktis.org.newtestapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Untuk memuat gambar dari URL atau URI

class BarangAdapter(private val barangList: MutableList<Barang>) : // Menggunakan MutableList
    RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false) // Menggunakan item_barang.xml
        return BarangViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val currentItem = barangList[position]
        holder.namaBarang.text = currentItem.nama
        // Menampilkan lebih banyak detail di item list (sesuaikan dengan item_barang.xml Anda)
        holder.stokBarang.text = "Stok: ${currentItem.stok} | Size: ${currentItem.size.ifEmpty { "-" }} | Harga: Rp ${String.format("%,.0f", currentItem.harga).replace(',', '.')}"


        // Memuat gambar barang
        if (!currentItem.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(currentItem.imageUrl)
                .placeholder(R.mipmap.ic_launcher_round) // Gambar placeholder saat memuat
                .error(R.drawable.broken_image) // Gambar jika gagal memuat (pastikan drawable ini ada)
                .into(holder.gambarBarang)
        } else {
            holder.gambarBarang.setImageResource(R.mipmap.ic_launcher_round) // Placeholder default jika tidak ada URL
        }

        // Menambahkan OnClickListener ke seluruh item view
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ItemDetailActivity.EXTRA_BARANG, currentItem) // Mengirim objek Barang (harus Parcelable)
                putExtra(ItemDetailActivity.EXTRA_BARANG_POSITION, position) // Mengirim posisi item
            }

            // Jika Activity yang memanggil adalah AllItemsActivity dan perlu menangani hasil
            if (context is AllItemsActivity) {
                context.itemDetailLauncher.launch(intent)
            } else {
                // Fallback jika dipanggil dari context lain (jarang terjadi untuk adapter ini)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = barangList.size

    // Fungsi untuk menghapus item dari adapter (dipanggil dari AllItemsActivity setelah repository diupdate)
    fun removeItem(position: Int) {
        if (position >= 0 && position < barangList.size) {
            barangList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, barangList.size)
        }
    }

    // Fungsi untuk mengupdate item di adapter (dipanggil dari AllItemsActivity setelah repository diupdate)
    fun updateItem(position: Int, barang: Barang) {
        if (position >= 0 && position < barangList.size) {
            barangList[position] = barang
            notifyItemChanged(position)
        }
    }

    class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gambarBarang: ImageView = itemView.findViewById(R.id.imageViewBarang)
        val namaBarang: TextView = itemView.findViewById(R.id.textViewNamaBarang)
        val stokBarang: TextView = itemView.findViewById(R.id.textViewStokBarang)
        // Inisialisasi view lain dari item_barang.xml jika ada (misal tombol aksi)
    }
}
