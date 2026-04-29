package com.example.jjjjjppppp.fragments

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.network.dto.ChatMessageDto

class ChatAdapter(
    private val messages: MutableList<ChatMessageDto>,
    private val currentUser: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llBubble: LinearLayout = itemView.findViewById(R.id.llBubble)
        val tvContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        val isMine = msg.fromUser == currentUser

        holder.tvContent.text = msg.content
        holder.tvTime.text = msg.createdAt

        val lp = holder.llBubble.layoutParams as LinearLayout.LayoutParams
        if (isMine) {
            lp.gravity = Gravity.END
            holder.llBubble.setBackgroundResource(R.drawable.bg_chat_bubble_self)
            holder.tvContent.setTextColor(Color.WHITE)
            holder.tvTime.setTextColor(Color.parseColor("#AAFFFFFF"))
        } else {
            lp.gravity = Gravity.START
            holder.llBubble.setBackgroundResource(R.drawable.bg_chat_bubble_other)
            holder.tvContent.setTextColor(Color.parseColor("#333333"))
            holder.tvTime.setTextColor(Color.parseColor("#999999"))
        }
        holder.llBubble.layoutParams = lp
    }

    override fun getItemCount(): Int = messages.size
}
