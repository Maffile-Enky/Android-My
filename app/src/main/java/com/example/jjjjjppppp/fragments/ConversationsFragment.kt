package com.example.jjjjjppppp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.dto.ConversationDto
import kotlinx.coroutines.launch

class ConversationsFragment : Fragment() {

    private lateinit var rvConversations: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var adapter: ConversationAdapter
    private val conversations = mutableListOf<ConversationDto>()

    private val currentUser: String
        get() {
            val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
            return prefs.getString("current_user", getString(R.string.anonymous_user))
                ?: getString(R.string.anonymous_user)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvConversations = view.findViewById(R.id.rvConversations)
        progressBar = view.findViewById(R.id.progressConversations)
        tvEmpty = view.findViewById(R.id.tvConvEmpty)
        tvError = view.findViewById(R.id.tvConvError)
        val ibBack: ImageButton = view.findViewById(R.id.ibConvBack)

        rvConversations.layoutManager = LinearLayoutManager(requireContext())

        adapter = ConversationAdapter(conversations) { conv ->
            val bundle = Bundle().apply {
                putString(ChatFragment.ARG_TARGET_USER, conv.targetUser)
            }
            findNavController().navigate(R.id.nav_chat, bundle)
        }
        rvConversations.adapter = adapter

        ibBack.setOnClickListener {
            findNavController().popBackStack()
        }

        loadConversations()
    }

    private fun loadConversations() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            tvError.visibility = View.GONE

            try {
                val response = RetrofitClient.apiService.getConversations(currentUser)
                if (response.isSuccessful) {
                    val list = response.body()?.conversations ?: emptyList()
                    conversations.clear()
                    conversations.addAll(list)
                    adapter.notifyDataSetChanged()

                    if (list.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    tvError.text = "HTTP ${response.code()}"
                    tvError.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvError.text = e.message
                tvError.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
