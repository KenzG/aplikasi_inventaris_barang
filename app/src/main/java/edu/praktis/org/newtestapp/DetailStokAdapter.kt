package edu.praktis.org.newtestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetailStokAdapter(
    private val itemList: List<Barang>,
    private val itemBackgroundColor: Int
) : RecyclerView.Adapter<DetailStokAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaBarang: TextView = view.findViewById(R.id.tv_nama_item)
        val hargaBarang: TextView = view.findViewById(R.id.tv_harga_item)
        val stokBarang: TextView = view.findViewById(R.id.tv_stok_item)
        val cardView: CardView = view.findViewById(R.id.card_item)
        val gambarItem: ImageView = view.findViewById(R.id.iv_gambar_item) // Deklarasi ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stok_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val barang = itemList[position]

        holder.namaBarang.text = barang.nama
        holder.hargaBarang.text = "Rp. ${"%,.0f".format(barang.harga).replace(',', '.')},-"
        holder.stokBarang.text = barang.stok.toString()

        holder.cardView.setCardBackgroundColor(itemBackgroundColor)

        // Logika untuk memuat gambar
        Glide.with(holder.itemView.context)
            .load(barang.imageUrl)
            .placeholder(R.drawable.sepatu3)
            .error(R.drawable.broken_image)
            .into(holder.gambarItem)
    }

    override fun getItemCount() = itemList.size
}