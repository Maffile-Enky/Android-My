package com.example.jjjjjppppp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    companion object {
        const val ARG_TARGET_USER = "target_user"
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var rvMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var ibSend: ImageButton
    private lateinit var ibBack: ImageButton
    private lateinit var tvTargetName: TextView
    private lateinit var progressChat: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<com.example.jjjjjppppp.network.dto.ChatMessageDto>()

    private val targetUser: String
        get() = arguments?.getString(ARG_TARGET_USER) ?: "unknown"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        rvMessages = view.findViewById(R.id.rvChatMessages)
        etInput = view.findViewById(R.id.etChatInput)
        ibSend = view.findViewById(R.id.ibSend)
        ibBack = view.findViewById(R.id.ibChatBack)
        tvTargetName = view.findViewById(R.id.tvChatTargetName)
        progressChat = view.findViewById(R.id.progressChat)
        tvError = view.findViewById(R.id.tvChatError)

        tvTargetName.text = targetUser
        rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        adapter = ChatAdapter(messages, viewModel.currentUser)
        rvMessages.adapter = adapter

        ibBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ibSend.setOnClickListener {
            val content = etInput.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(content)
                etInput.text.clear()
            }
        }

        observeViewModel()
        viewModel.init(targetUser)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressChat.visibility = View.GONE
                tvError.visibility = View.GONE
                when (state) {
                    is ChatViewModel.UiState.Loading -> {
                        if (messages.isEmpty()) progressChat.visibility = View.VISIBLE
                    }
                    is ChatViewModel.UiState.Error -> {
                        tvError.text = state.message
                        tvError.visibility = View.VISIBLE
                    }
                    is ChatViewModel.UiState.Idle -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { newMessages ->
                messages.clear()
                messages.addAll(newMessages)
                adapter.notifyDataSetChanged()
                if (newMessages.isNotEmpty()) {
                    rvMessages.smoothScrollToPosition(newMessages.size - 1)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnect()
    }
}
