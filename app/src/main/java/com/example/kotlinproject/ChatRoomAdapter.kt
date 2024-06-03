package com.example.kotlinproject

import ChatRoom
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ChatRoomAdapter(
    private val context: Context,
    private val chatRooms: ArrayList<ChatRoom>,
    private val currentUserUid: String,
    private val onItemClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.VH>() {

    private val db = FirebaseFirestore.getInstance()

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendProfileImage: ImageView = itemView.findViewById(R.id.friendProfileImage)
        val friendName: TextView = itemView.findViewById(R.id.friendName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val lastMessageTime: TextView = itemView.findViewById(R.id.lastMessageTime)

        init {
            itemView.setOnClickListener {
                onItemClick(chatRooms[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.chat_room_item, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chatRoom = chatRooms[position]
        holder.friendName.text = chatRoom.friendName
        holder.lastMessage.text = chatRoom.lastMessage
        holder.lastMessageTime.text = chatRoom.lastMessageTime

        db.collection("user").document(chatRoom.friendUID).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val profileUrl = document.getString("profileUrl")
                    if (!profileUrl.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(profileUrl)
                            .into(holder.friendProfileImage)
                    } else {
                        holder.friendProfileImage.setImageResource(R.drawable.group_112)
                    }
                }
            }
            .addOnFailureListener {
                // Handle the error, set default image or placeholder
                holder.friendProfileImage.setImageResource(R.drawable.group_112)
            }
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }
}
