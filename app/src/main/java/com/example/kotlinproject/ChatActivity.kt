package com.example.kotlinproject

import android.content.Context
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friendNameText = findViewById(R.id.friendNameText)
        backButton = findViewById(R.id.backButton)
        messageText = findViewById(R.id.messageText)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)


        messageAdapter = MessageAdapter(this, messageItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        val friendName: String = intent.getStringExtra("friendName").toString()
        val friendUID: String = intent.getStringExtra("friendUID").toString()
        myName = intent.getStringExtra("myName").toString()
        Profile.myName = myName
        friendNameText.text = friendName

        db.collection("user").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {querySnapshot->
                Profile.myProfileUrl = querySnapshot.getString("profileUrl")
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

                messageItems.add(Message(name, message, profileUrl, time))
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
        val name = myName
        val message = messageText.text.toString()
        val time = "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:${Calendar.getInstance().get(Calendar.MINUTE)}"
        val profileUrl = Profile.myProfileUrl.toString()

        if (message.isEmpty()) {
            Toast.makeText(this, "메시지를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Message(name, message, profileUrl, time)
        chatRef.document("MSG_${System.currentTimeMillis()}").set(item)

        messageText.setText("")

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}