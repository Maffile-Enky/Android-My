package com.example.jjjjjppppp.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityFragment : Fragment() {

    private lateinit var rvCommunityPosts: RecyclerView
    private lateinit var fabNewPost: FloatingActionButton
    private lateinit var adapter: ForumPostAdapter
    private val posts = mutableListOf<ForumPostAdapter.ForumPost>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCommunityPosts = view.findViewById(R.id.rvCommunityPosts)
        fabNewPost = view.findViewById(R.id.fabNewPost)

        rvCommunityPosts.layoutManager = LinearLayoutManager(requireContext())

        adapter = ForumPostAdapter(
            posts,
            onLikeClick = { position -> toggleLike(position) },
            onPostClick = { position -> showPostDetailDialog(position) }
        )
        rvCommunityPosts.adapter = adapter

        loadPosts()
        if (posts.isEmpty()) {
            seedMockData()
        }

        fabNewPost.setOnClickListener {
            showNewPostDialog()
        }
    }

    private fun loadPosts() {
        val prefs = requireContext().getSharedPreferences("community_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("forum_posts", null) ?: return
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                posts.add(
                    ForumPostAdapter.ForumPost(
                        id = obj.getLong("id"),
                        authorName = obj.getString("authorName"),
                        time = obj.getString("time"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        likeCount = obj.getInt("likeCount")
                    )
                )
            }
        } catch (_: Exception) { }
    }

    private fun savePosts() {
        val arr = JSONArray()
        for (post in posts) {
            val obj = JSONObject()
            obj.put("id", post.id)
            obj.put("authorName", post.authorName)
            obj.put("time", post.time)
            obj.put("title", post.title)
            obj.put("content", post.content)
            obj.put("likeCount", post.likeCount)
            arr.put(obj)
        }
        requireContext().getSharedPreferences("community_prefs", Context.MODE_PRIVATE)
            .edit().putString("forum_posts", arr.toString()).apply()
    }

    private fun seedMockData() {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = Date()
        val mockPosts = listOf(
            ForumPostAdapter.ForumPost(1, getString(R.string.sample_author_admin), formatTime(timeFormat, now, -9), getString(R.string.sample_title_welcome), getString(R.string.sample_content_welcome), 5),
            ForumPostAdapter.ForumPost(2, getString(R.string.sample_author_xiaoming), formatTime(timeFormat, now, -7), getString(R.string.sample_title_counter), getString(R.string.sample_content_counter), 3),
            ForumPostAdapter.ForumPost(3, getString(R.string.sample_author_zhangsan), formatTime(timeFormat, now, -4), getString(R.string.sample_title_feature), getString(R.string.sample_content_feature), 8),
            ForumPostAdapter.ForumPost(4, getString(R.string.sample_author_fitness), formatTime(timeFormat, now, -3), getString(R.string.sample_title_bmi), getString(R.string.sample_content_bmi), 2),
            ForumPostAdapter.ForumPost(5, getString(R.string.sample_author_daily), formatTime(timeFormat, now, -1), getString(R.string.sample_title_checkin), getString(R.string.sample_content_checkin), 12)
        )
        posts.addAll(mockPosts)
        savePosts()
    }

    private fun formatTime(format: SimpleDateFormat, now: Date, daysAgo: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.time = now
        cal.add(java.util.Calendar.DAY_OF_YEAR, daysAgo)
        return format.format(cal.time)
    }

    private fun toggleLike(position: Int) {
        val post = posts[position]
        post.isLiked = !post.isLiked
        post.likeCount = if (post.isLiked) post.likeCount + 1 else post.likeCount - 1
        adapter.notifyItemChanged(position)
        savePosts()
    }

    private fun showNewPostDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_post, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etPostTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etPostContent)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.publish_btn)) { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.enter_post_title), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (content.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.enter_post_content), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
                val author = prefs.getString("current_user", getString(R.string.anonymous_user)) ?: getString(R.string.anonymous_user)
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val newId = (posts.maxOfOrNull { it.id } ?: 0) + 1

                posts.add(
                    0,
                    ForumPostAdapter.ForumPost(
                        id = newId,
                        authorName = author,
                        time = timeFormat.format(Date()),
                        title = title,
                        content = content,
                        likeCount = 0
                    )
                )
                adapter.notifyItemInserted(0)
                rvCommunityPosts.scrollToPosition(0)
                savePosts()
                Toast.makeText(requireContext(), getString(R.string.publish_success), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showPostDetailDialog(position: Int) {
        val post = posts[position]
        val message = getString(R.string.post_detail_format, post.authorName, post.time, post.content)
        AlertDialog.Builder(requireContext())
            .setTitle(post.title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.close), null)
            .show()
    }
}
