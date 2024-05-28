package com.example.kotlinproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class MessageAdapter(private val context: Context, private val messageItems: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.VH>() {

    companion object {
        const val TYPE_MY = 0
        const val TYPE_OTHER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageItems[position].name == Profile.myName) {
            TYPE_MY
        } else {
            TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView: View = if (viewType == TYPE_MY) {
            LayoutInflater.from(context).inflate(R.layout.my_message_box, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.friend_message_box, parent, false)
        }
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = messageItems[position]
        holder.name.text = item.name
        holder.message.text = item.message
        holder.time.text = item.time
        Glide.with(context).load(item.profileUrl).into(holder.profileImage)
    }

    override fun getItemCount(): Int {
        return messageItems.size
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val name: TextView = itemView.findViewById(R.id.name)
        val message: TextView = itemView.findViewById(R.id.message)
        val time: TextView = itemView.findViewById(R.id.time)
    }
}