package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class SubActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerMenuButton: ImageView
    private lateinit var logoutButton: TextView
    private lateinit var editFindFriend: EditText
    private lateinit var chatContainer: LinearLayout

    private lateinit var friendUID: String

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        drawerMenuButton = findViewById(R.id.drawer_menu)
        logoutButton = findViewById(R.id.logoutButton)
        editFindFriend = findViewById(R.id.editFindFriend)
        chatContainer = findViewById(R.id.chatContainer)

        if (auth.currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        drawerMenuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        logoutButton.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        editFindFriend.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                val friendName: String = editFindFriend.text.toString()

                if (friendName.isNotEmpty()) {
                    db.collection("user")
                        .whereEqualTo("UID", auth.currentUser?.uid)
                        .get()
                        .addOnSuccessListener { querySnapshot->
                            val myName: String = querySnapshot.documents[0].getString("name").toString()
                            if (myName != friendName) {
                                searchFriend(friendName)
                            } else {
                                Toast.makeText(this, "자기 자신의 이름은 입력할 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                true
            } else {
                false
            }
        }
    }

    private fun searchFriend(friendName: String) {
        db.collection("user")
            .whereEqualTo("name", friendName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "친구를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val document = documents.documents[0]
                    friendUID = document.getString("UID").toString()
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("friendName", friendName)
                    intent.putExtra("friendUID", friendUID)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "에러 발생: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
