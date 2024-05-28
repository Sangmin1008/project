package com.example.kotlinproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide

class ProfileActivity: AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var editName: EditText
    private lateinit var editPassword: EditText
    private lateinit var currentPassword: EditText
    private lateinit var editProfileButton: TextView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var profileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        editName = findViewById(R.id.editName)
        editPassword = findViewById(R.id.editPassword)
        currentPassword = findViewById(R.id.currentPassword)
        editProfileButton = findViewById(R.id.editProfileButton)

        db.collection("user").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot->
                editName.setText(querySnapshot.getString("name").toString())
                val uri = querySnapshot.getString("profileUrl")
                if (!uri.isNullOrEmpty()) {
                    Glide.with(this).load(uri).into(profileImage)
                }
                //editPassword.setText(querySnapshot.documents[0].getString("password").toString())
            }

        editName.setText(Profile.myName.toString())

        backButton.setOnClickListener {
            finish()
        }

        profileImage.setOnClickListener {
            openGallery()
        }

        editProfileButton.setOnClickListener {
            val newPassword = editPassword.text.toString()
            val newName = editName.text.toString()
            val currentPwd = currentPassword.text.toString()

            if (newPassword.isNotEmpty() && newName.isNotEmpty() && currentPwd.isNotEmpty()) {
                updateProfile(newName, newPassword, currentPwd)
            } else {
                Toast.makeText(this, "입력을 다시 한 번 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                profileImage.setImageURI(imageUri)
                profileUri = imageUri
            }
        }
    }

    private fun updateProfile(newName: String, newPassword: String, currentPwd: String) {
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
                                            "name" to newName,
                                            "password" to newPassword
                                        ))
                                            .addOnSuccessListener {
                                                uploadImage()
                                                Toast.makeText(this, "회원정보가 성공적으로 수정되었습니다", Toast.LENGTH_SHORT).show()
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


    private fun uploadImage() {
        if (profileUri == null) return

        val imgRef = storage.getReference("profileImg/IMG_${auth.currentUser!!.uid}")
        imgRef.putFile(profileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                imgRef.downloadUrl.addOnSuccessListener { uri ->
                    Profile.myProfileUrl = uri.toString()

                    val profileRef = db.collection("user").document(auth.currentUser!!.uid)
                    profileRef.update("profileUrl", Profile.myProfileUrl)
                }
            }
    }

}