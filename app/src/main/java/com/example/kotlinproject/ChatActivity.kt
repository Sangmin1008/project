package com.example.kotlinproject

import ChatRoom
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ChatActivity: AppCompatActivity() {
    private lateinit var friendNameText: TextView
    private lateinit var backButton: ImageView
    private lateinit var messageText: TextView
    private lateinit var sendButton: TextView
    private lateinit var recyclerView: RecyclerView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var chatRef: CollectionReference

    private lateinit var roomID: String
    private val messageItems = ArrayList<Message>()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var myName: String
    private lateinit var friendName: String
    private lateinit var  friendUID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendNameText = findViewById(R.id.friendNameText)
        backButton = findViewById(R.id.backButton)
        messageText = findViewById(R.id.messageText)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)


        messageAdapter = MessageAdapter(this, messageItems, auth.currentUser!!.uid)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        friendName = intent.getStringExtra("friendName").toString()
        friendUID = intent.getStringExtra("friendUID").toString()
        friendNameText.text = friendName

        db.collection("user").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {querySnapshot->
                Profile.myProfileUrl = querySnapshot.getString("profileUrl")
                myName = querySnapshot.getString("name").toString()
            }

        roomID = if (friendUID > auth.uid.toString()) {
            friendUID + auth.uid.toString()
        } else {
            auth.uid.toString() + friendUID
        }

        chatRef = db.collection(roomID)
        chatRef.addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener

            value?.documentChanges?.forEach { documentChange ->
                val snapshot = documentChange.document
                val msg = snapshot.data
                val name = msg["name"].toString()
                val message = msg["message"].toString()
                val profileUrl = msg["profileUrl"].toString()
                val time = msg["time"].toString()
                val uid = msg["uid"].toString()

                messageItems.add(Message(name, message, profileUrl, time, uid))
                messageAdapter.notifyItemInserted(messageItems.size - 1)
                recyclerView.scrollToPosition(messageItems.size - 1)
            }
        }

        sendButton.setOnClickListener {
            sendMessage()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun sendMessage() {
        val name = Profile.myName.toString()
        val message = messageText.text.toString()
        var time = "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:${Calendar.getInstance().get(Calendar.MINUTE)}"
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10) {
            time = "0$time"
        }
        if (Calendar.getInstance().get(Calendar.MINUTE) < 10) {
            time = "${time}0"
        }
        val profileUrl = Profile.myProfileUrl.toString()
        val uid = auth.currentUser!!.uid

        if (message.isEmpty()) {
            Toast.makeText(this, "메시지를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Message(name, message, profileUrl, time, uid)
        chatRef.document("MSG_${System.currentTimeMillis()}").set(item)

        messageText.setText("")

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val myChatRoom = ChatRoom(roomID, friendNameText.text.toString(), friendUID, message, time)
        val friendChatRoom = ChatRoom(roomID, myName, auth.currentUser!!.uid, message, time)
        db.collection("chatRooms").document(auth.currentUser!!.uid)
            .collection("rooms").document(roomID).set(myChatRoom)
        db.collection("chatRooms").document(friendUID)
            .collection("rooms").document(roomID).set(friendChatRoom)
    }
}