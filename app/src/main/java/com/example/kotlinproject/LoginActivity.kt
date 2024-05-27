package com.example.kotlinproject

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedStateListDrawable
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var editEmail : EditText
    private lateinit var editPassword : EditText
    private lateinit var loginButton : TextView
    private lateinit var signButton : TextView
    private lateinit var loginRememberCheck : CheckBox
    private lateinit var appData: SharedPreferences

    private var saveLoginData : Boolean = false
    private var email: String? = null
    private var pwd: String? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        backButton = findViewById(R.id.backButton)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        loginButton = findViewById(R.id.loginButton)
        signButton = findViewById(R.id.signButton)
        loginRememberCheck = findViewById(R.id.loginRememberCheck)

        appData = getSharedPreferences("appData", MODE_PRIVATE)
        load()

        if (saveLoginData) {
            editEmail.setText(email)
            editPassword.setText(pwd)
            loginRememberCheck.isChecked = saveLoginData
        }

        backButton.setOnClickListener {
            finish()
        }

        loginButton.setOnClickListener {
            login()
        }

        signButton.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    public override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login() {
        val id : String = editEmail.text.toString().trim()
        val password : String = editPassword.text.toString().trim()

        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(id, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "로그인 오류", Toast.LENGTH_SHORT).show()
                }
            }
        save()
    }

    private fun save() {
        val editor = appData.edit()
        editor.putBoolean("SAVE_LOGIN_DATA", loginRememberCheck.isChecked)
        editor.putString("ID", editEmail.text.toString().trim())
        editor.putString("PWD", editPassword.text.toString().trim())
        editor.apply()
    }

    private fun load() {
        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false)
        email = appData.getString("ID", "") ?: ""
        pwd = appData.getString("PWD", "") ?: ""
    }
}