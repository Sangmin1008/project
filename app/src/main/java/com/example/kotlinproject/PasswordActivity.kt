package com.example.kotlinproject

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PasswordActivity: AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var editPassword: EditText
    private lateinit var currentPassword: EditText
    private lateinit var editPasswordButton: TextView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        backButton = findViewById(R.id.backButton)
        editPassword = findViewById(R.id.editPassword)
        currentPassword = findViewById(R.id.currentPassword)
        editPasswordButton = findViewById(R.id.editPasswordButton)

        backButton.setOnClickListener {
            finish()
        }

        editPasswordButton.setOnClickListener {
            val newPassword = editPassword.text.toString()
            val currentPwd = currentPassword.text.toString()

            if (newPassword.isNotEmpty() && currentPwd.isNotEmpty()) {
                updateProfile(newPassword, currentPwd)
            } else {
                Toast.makeText(this, "입력을 다시 한 번 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile(newPassword: String, currentPwd: String) {
        val user = auth.currentUser
        if (user != null) {
            val userRef = db.collection("user").document(user.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val storedPassword = document.getString("password")
                        if (storedPassword == currentPwd) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        userRef.update(mapOf(
                                            "password" to newPassword
                                        ))
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "비밀번호가 성공적으로 수정되었습니다", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Firestore 업데이트 에러발생", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(this, "비밀번호 업데이트 에러발생", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "현재 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 가져오는 데 실패했습니다", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "존재하지 않는 계정", Toast.LENGTH_SHORT).show()
        }
    }
}