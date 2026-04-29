package com.example.jjjjjppppp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.network.dto.ConversationDto

class ConversationAdapter(
    private val conversations: MutableList<ConversationDto>,
    private val onClick: (ConversationDto) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConvViewHolder>() {

    inner class ConvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvConvName)
        val tvPreview: TextView = itemView.findViewById(R.id.tvConvPreview)
        val tvTime: TextView = itemView.findViewById(R.id.tvConvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConvViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConvViewHolder, position: Int) {
        val conv = conversations[position]
        holder.tvName.text = conv.targetUser

        val prefix = if (conv.isFromMe)
            holder.itemView.context.getString(R.string.msg_preview_self, "")
        else ""
        holder.tvPreview.text = "$prefix${conv.lastMessage}"
        holder.tvTime.text = conv.lastTime.takeLast(11) // "MM-dd HH:mm"

        holder.itemView.setOnClickListener { onClick(conv) }
    }

    override fun getItemCount(): Int = conversations.size
}
