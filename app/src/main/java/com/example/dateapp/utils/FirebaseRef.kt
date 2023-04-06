package com.example.dateapp.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRef {
    companion object {
        // Firebase 의 유저 정보 가져오기
        val database = Firebase.database
        val userInfoRef = database.getReference("userInfo")
        val userLikeRef = database.getReference("userLike")
        val userMessageRef = database.getReference("userMessage")
    }
}