package com.example.jjjjjppppp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R

class BannerAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private var items: List<Pair<String, String>> = emptyList() // title to content

    var currentPosition = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setItems(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.banner_item, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val realPos = position % items.size.coerceAtLeast(1)
        val item = items[realPos]
        holder.bind(item.first, item.second)
        holder.itemView.setOnClickListener { onItemClick(realPos) }
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 0 else Int.MAX_VALUE // infinite scroll

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBannerImage: ImageView = itemView.findViewById(R.id.ivBannerImage)
        private val tvBannerText: TextView = itemView.findViewById(R.id.tvBannerText)

        fun bind(title: String, content: String) {
            tvBannerText.text = "$title\n$content"
            ivBannerImage.setImageResource(R.mipmap.ic_launcher)
        }
    }
}
