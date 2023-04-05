package com.example.dateapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.dateapp.MainActivity
import com.example.dateapp.R
import com.example.dateapp.databinding.ActivityJoinBinding
import com.example.dateapp.utils.FirebaseRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    private var nickname = ""
    private var city = ""
    private var age = ""
    private var gender = ""
    private var uid = ""

    private val TAG = "JoinActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        auth = Firebase.auth

        // 회원가입 버튼 클릭 시
        binding.joinBtn.setOnClickListener {

            nickname = binding.nickname.text.toString()
            age = binding.age.text.toString()
            gender = binding.gender.text.toString()
            city = binding.city.text.toString()

            auth.createUserWithEmailAndPassword(
                binding.email.text.toString(),
                binding.pwd.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        uid = user?.uid.toString()

                        val userModel = UserDataModel(uid, nickname, age, gender, city)

                        FirebaseRef.userInfoRef.child(uid).setValue(userModel)

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {

                    }
                }
        }
    }
}