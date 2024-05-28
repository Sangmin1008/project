package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SignActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var signButton: TextView
    private lateinit var loginButton: TextView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        backButton = findViewById(R.id.backButton)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        signButton = findViewById(R.id.signButton)
        loginButton = findViewById(R.id.loginButton)

        if (auth.currentUser != null) {
            val intent = Intent(this, SubActivity::class.java)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }

        signButton.setOnClickListener {
            sign()
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            //finish()
        }
    }

    private fun sign() {
        val name: String = editName.text.toString().trim()
        val email: String = editEmail.text.toString().trim()
        val password: String = editPassword.text.toString().trim()

        if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
            auth.createUserWithEmailAndPassword(editEmail.text.toString(), editPassword.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    user?.let {
                        val userMap = hashMapOf(
                            "UID" to it.uid,
                            "name" to name,
                            "email" to email,
                            "password" to password,
                            "profileUrl" to ""
                        )

                        val colRef: CollectionReference = db.collection("user")
                        val docRef: Task<Void> = colRef.document(it.uid).set(userMap)

                        docRef.addOnSuccessListener {
                            Toast.makeText(this, "회원가입 성공.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "데이터 저장 실패.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "회원가입 실패했습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "입력정보를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
        }
    }

}