package com.example.jjjjjppppp.fragments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R

class ForumPostAdapter(
    private val posts: MutableList<ForumPost>,
    private val onLikeClick: (Int) -> Unit,
    private val onPostClick: (Int) -> Unit
) : RecyclerView.Adapter<ForumPostAdapter.PostViewHolder>() {

    data class ForumPost(
        val id: Long,
        val authorName: String,
        val time: String,
        val title: String,
        val content: String,
        var likeCount: Int,
        var isLiked: Boolean = false
    )

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivPostAvatar)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        private val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        private val tvPostTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        private val tvPostContent: TextView = itemView.findViewById(R.id.tvPostContent)
        private val btnLike: Button = itemView.findViewById(R.id.btnLike)

        fun bind(post: ForumPost, position: Int) {
            tvAuthorName.text = post.authorName
            tvPostTime.text = post.time
            tvPostTitle.text = post.title
            tvPostContent.text = post.content
            updateLikeButton(post)

            btnLike.setOnClickListener { onLikeClick(position) }
            itemView.setOnClickListener { onPostClick(position) }
        }

        fun updateLikeButton(post: ForumPost) {
            val symbol = if (post.isLiked) "♥" else "♡"
            val color = if (post.isLiked) Color.parseColor("#F44336") else Color.parseColor("#999999")
            btnLike.text = "$symbol ${post.likeCount}"
            btnLike.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forum_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], position)
    }

    override fun getItemCount(): Int = posts.size
}
