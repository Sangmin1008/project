package com.example.kotlinproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerMenuButton: ImageView
    private lateinit var loginActivityButton : TextView
    private lateinit var loginActivityButton2 : TextView
    private lateinit var signText: TextView

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        drawerMenuButton = findViewById(R.id.drawer_menu)
        loginActivityButton = findViewById(R.id.loginActivityButton)
        loginActivityButton2 = findViewById(R.id.loginActivityButton2)
        signText = findViewById(R.id.signText)

        if (auth.currentUser != null) {
            val intent = Intent(this, SubActivity::class.java)
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

        loginActivityButton.setOnClickListener {
            val user = auth.currentUser

            if (user != null) {
                Toast.makeText(this, "이미 로그인 상태입니다.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                //finish()
            }
        }

        loginActivityButton2.setOnClickListener {
            val user = auth.currentUser

            if (user != null) {
                Toast.makeText(this, "이미 로그인 상태입니다.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                //finish()
            }
        }

        signText.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}