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
import com.example.jjjjjppppp.network.dto.PostDto

class ForumPostAdapter(
    private val posts: MutableList<PostDto>,
    private val onLikeClick: (Int) -> Unit,
    private val onPostClick: (Int) -> Unit,
    private val onAvatarClick: (Int) -> Unit
) : RecyclerView.Adapter<ForumPostAdapter.PostViewHolder>() {

    // Track liked post IDs locally
    private val likedPostIds = mutableSetOf<Long>()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivPostAvatar)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        private val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        private val tvPostTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        private val tvPostContent: TextView = itemView.findViewById(R.id.tvPostContent)
        private val btnLike: Button = itemView.findViewById(R.id.btnLike)

        fun bind(post: PostDto, position: Int) {
            tvAuthorName.text = post.author
            tvPostTime.text = post.createdAt
            tvPostTitle.text = post.title
            tvPostContent.text = post.content
            updateLikeButton(post)

            ivAvatar.setOnClickListener { onAvatarClick(position) }
            btnLike.setOnClickListener {
                onLikeClick(position)
                toggleLocalLike(post)
            }
            itemView.setOnClickListener { onPostClick(position) }
        }

        private fun toggleLocalLike(post: PostDto) {
            if (likedPostIds.contains(post.id)) {
                likedPostIds.remove(post.id)
            } else {
                likedPostIds.add(post.id)
            }
            updateLikeButton(post)
        }

        private fun updateLikeButton(post: PostDto) {
            val isLiked = likedPostIds.contains(post.id)
            // Adjust display count for local like toggle
            val displayLikes = if (isLiked) post.likes + 1 else post.likes
            val symbol = if (isLiked) "♥" else "♡"
            val color = if (isLiked) Color.parseColor("#F44336") else Color.parseColor("#999999")
            btnLike.text = "$symbol $displayLikes"
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
