package com.example.jjjjjppppp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R

class BannerAdapter(
    private val announcements: ArrayList<HomeFragment.Announcement>
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    var currentPosition = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.banner_item, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.bind(announcement, position == currentPosition)
    }

    override fun getItemCount(): Int = announcements.size

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBannerImage: ImageView = itemView.findViewById(R.id.ivBannerImage)
        private val tvBannerText: TextView = itemView.findViewById(R.id.tvBannerText)
        private val ivLogoMark: ImageView = itemView.findViewById(R.id.ivLogoMark)

        fun bind(announcement: HomeFragment.Announcement, isCurrent: Boolean) {
            try {
                if (announcement.isLogoPage) {
                    // Logo页面显示
                    tvBannerText.text = "${announcement.title}\n${announcement.description}"
                    ivLogoMark.visibility = View.VISIBLE
                    ivBannerImage.setImageResource(R.mipmap.ic_launcher)
                } else {
                    // 普通公告页面
                    tvBannerText.text = announcement.description
                    ivLogoMark.visibility = View.GONE
                    ivBannerImage.setImageResource(announcement.imageRes)
                }

                // 根据是否是当前页面更新样式
                itemView.alpha = if (isCurrent) 1.0f else 0.7f
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}