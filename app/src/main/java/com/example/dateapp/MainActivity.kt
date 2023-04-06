package com.example.dateapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dateapp.auth.UserDataModel
import com.example.dateapp.setting.SettingActivity
import com.example.dateapp.slider.CardStackAdapter
import com.example.dateapp.utils.FirebaseAuthUtils
import com.example.dateapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class MainActivity : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var cardStackLayoutManager: CardStackLayoutManager

    private val usersDataList = mutableListOf<UserDataModel>()

    private val TAG = MainActivity::class.java.simpleName

    private var userCount = 0

    private lateinit var currentUserGender: String

    private val uid = FirebaseAuthUtils.getUid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val setting = findViewById<ImageView>(R.id.settingIcon)
        setting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)

        cardStackLayoutManager = CardStackLayoutManager(baseContext, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {
                if (direction == Direction.Right) {
                    Toast.makeText(this@MainActivity, "right", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, usersDataList[userCount].uid.toString())

                    userLike(uid, usersDataList[userCount].uid.toString())
                }

                if (direction == Direction.Left) {
                    Toast.makeText(this@MainActivity, "left", Toast.LENGTH_SHORT).show()
                }
                userCount += 1

                if (userCount == usersDataList.count()) {
                    getUserDataList(currentUserGender)
                    Toast.makeText(this@MainActivity, "유저를 새롭게 받아 옴.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }

        })

        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = cardStackLayoutManager
        cardStackView.adapter = cardStackAdapter


        getMyUserData()
    }

    private fun getMyUserData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                Log.d(TAG, data?.gender.toString())

                currentUserGender = data?.gender.toString()

                getUserDataList(currentUserGender)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    private fun getUserDataList(currentUserGender: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataModel in dataSnapshot.children) {
                    val user = dataModel.getValue(UserDataModel::class.java)

                    if (user!!.gender.toString().equals(currentUserGender)) {

                    } else {
                        usersDataList.add(user!!)
                    }
                }
                cardStackAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }

    // 사용자가 '좋아요' 를 나타내는 것
    private fun userLike(myUid: String, otherUid: String) {
        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")
    }
}