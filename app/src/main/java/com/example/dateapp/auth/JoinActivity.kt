package com.example.dateapp.auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.dateapp.MainActivity
import com.example.dateapp.R
import com.example.dateapp.databinding.ActivityJoinBinding
import com.example.dateapp.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    private var nickname = ""
    private var city = ""
    private var age = ""
    private var gender = ""
    private var uid = ""

    lateinit var profileImage: ImageView

    private val TAG = "JoinActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        auth = Firebase.auth

        profileImage = findViewById(R.id.image)

        // 이미지 파일을 가져오는 과정
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImage.setImageURI(uri)
            }
        )

        profileImage.setOnClickListener {
            getAction.launch("image/*")
        }


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

                        // Token
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(
                                        TAG,
                                        "Fetching FCM registration token failed",
                                        task.exception
                                    )
                                    return@OnCompleteListener
                                }

                                // Get new FCM registration token
                                val token = task.result

                                Log.d(TAG, token.toString())
                                val userModel = UserDataModel(uid, nickname, age, gender, city, token)

                                FirebaseRef.userInfoRef.child(uid).setValue(userModel)

                                uploadImage(uid)

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            })
                    } else {

                    }
                }
        }
    }

    // 이미지를 Firebase storage 에 업로드
    private fun uploadImage(uid: String) {
        // Firebase storage 연결
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")

        // Get the data from an ImageView as bytes
        profileImage.isDrawingCacheEnabled = true
        profileImage.buildDrawingCache()
        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}