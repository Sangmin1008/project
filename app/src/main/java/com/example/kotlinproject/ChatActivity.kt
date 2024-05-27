package com.example.kotlinproject

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity: AppCompatActivity() {
    private lateinit var friendNameText: TextView
    private lateinit var backButton: ImageView
    private lateinit var messageText: TextView
    private lateinit var sendButton: TextView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var collectionName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendNameText = findViewById(R.id.friendNameText)
        backButton = findViewById(R.id.backButton)
        messageText = findViewById(R.id.messageText)
        sendButton = findViewById(R.id.sendButton)

        val friendName: String = intent.getStringExtra("friendName").toString()
        val friendUID: String = intent.getStringExtra("friendUID").toString()
        friendNameText.text = friendName

        collectionName = if (friendUID > auth.uid.toString()) {
            friendUID + auth.uid.toString()
        } else {
            auth.uid.toString() + friendUID
        }

        backButton.setOnClickListener {
            finish()
        }


    }
}