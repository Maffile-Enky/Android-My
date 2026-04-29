package com.example.jjjjjppppp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.network.dto.PostDto
import com.example.jjjjjppppp.viewmodel.CommunityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var rvCommunityPosts: RecyclerView
    private lateinit var fabNewPost: FloatingActionButton
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressLoading: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var btnRetry: View
    private lateinit var adapter: ForumPostAdapter
    private val posts = mutableListOf<PostDto>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]

        rvCommunityPosts = view.findViewById(R.id.rvCommunityPosts)
        fabNewPost = view.findViewById(R.id.fabNewPost)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        progressLoading = view.findViewById(R.id.progressLoading)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvError = view.findViewById(R.id.tvError)
        btnRetry = view.findViewById(R.id.btnRetry)

        rvCommunityPosts.layoutManager = LinearLayoutManager(requireContext())

        adapter = ForumPostAdapter(
            posts,
            onLikeClick = { position -> viewModel.likePost(posts[position].id) },
            onPostClick = { position -> showPostDetailDialog(position) },
            onAvatarClick = { position -> navigateToChat(position) }
        )
        rvCommunityPosts.adapter = adapter

        swipeRefresh.setOnRefreshListener { viewModel.loadPosts() }
        btnRetry.setOnClickListener { viewModel.loadPosts() }
        fabNewPost.setOnClickListener { showNewPostDialog() }

        val ibMessages = view.findViewById<ImageButton>(R.id.ibMessages)
        ibMessages.setOnClickListener {
            findNavController().navigate(R.id.nav_conversations)
        }

        observeViewModel()
        viewModel.loadPosts()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressLoading.visibility = View.GONE
                tvEmpty.visibility = View.GONE
                tvError.visibility = View.GONE
                btnRetry.visibility = View.GONE

                when (state) {
                    is CommunityViewModel.UiState.Loading -> {
                        if (posts.isEmpty()) progressLoading.visibility = View.VISIBLE
                    }
                    is CommunityViewModel.UiState.Empty -> {
                        tvEmpty.visibility = View.VISIBLE
                    }
                    is CommunityViewModel.UiState.Success -> {
                        // RecyclerView shows posts
                    }
                    is CommunityViewModel.UiState.Error -> {
                        tvError.text = state.message
                        tvError.visibility = View.VISIBLE
                        btnRetry.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.posts.collect { newPosts ->
                posts.clear()
                posts.addAll(newPosts)
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWebSocket()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectWebSocket()
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
                val author = prefs.getString("current_user", getString(R.string.anonymous_user))
                    ?: getString(R.string.anonymous_user)

                viewModel.createPost(author, title, content)
                Toast.makeText(requireContext(), getString(R.string.publish_success), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateToChat(position: Int) {
        val post = posts[position]
        val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
        val currentUser = prefs.getString("current_user", getString(R.string.anonymous_user))
            ?: getString(R.string.anonymous_user)

        if (post.author == currentUser) {
            Toast.makeText(requireContext(), R.string.cannot_chat_self, Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply { putString(ChatFragment.ARG_TARGET_USER, post.author) }
        findNavController().navigate(R.id.nav_chat, bundle)
    }

    private fun showPostDetailDialog(position: Int) {
        val post = posts[position]
        val message = getString(R.string.post_detail_format, post.author, post.createdAt, post.content)
        AlertDialog.Builder(requireContext())
            .setTitle(post.title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.close), null)
            .show()
    }
}
