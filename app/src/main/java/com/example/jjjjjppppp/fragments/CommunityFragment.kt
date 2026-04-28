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
            ForumPostAdapter.ForumPost(1, "管理员", formatTime(timeFormat, now, -9), "欢迎来到社区", "这是第一条社区帖子，欢迎大家交流讨论工具使用心得！", 5),
            ForumPostAdapter.ForumPost(2, "小明", formatTime(timeFormat, now, -7), "计数器使用技巧", "长按+号可以快速增加，数据会自动保存不会丢失。", 3),
            ForumPostAdapter.ForumPost(3, "用户张三", formatTime(timeFormat, now, -4), "新功能建议", "希望增加深色模式切换功能和更多实用工具。", 8),
            ForumPostAdapter.ForumPost(4, "健身达人", formatTime(timeFormat, now, -3), "BMI计算器心得", "这个工具计算结果很准确，推荐大家使用！", 2),
            ForumPostAdapter.ForumPost(5, "日常用户", formatTime(timeFormat, now, -1), "打卡签到", "大家今天用了哪个工具？我用了倒计时煮泡面！", 12)
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
            .setPositiveButton("发布") { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "请输入标题", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (content.isEmpty()) {
                    Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
                val author = prefs.getString("current_user", "匿名用户") ?: "匿名用户"
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
                Toast.makeText(requireContext(), "发布成功", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showPostDetailDialog(position: Int) {
        val post = posts[position]
        val message = "作者：${post.authorName}\n时间：${post.time}\n\n${post.content}"
        AlertDialog.Builder(requireContext())
            .setTitle(post.title)
            .setMessage(message)
            .setPositiveButton("关闭", null)
            .show()
    }
}
