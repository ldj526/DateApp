package com.example.dateapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dateapp.auth.UserDataModel
import com.example.dateapp.setting.SettingActivity
import com.example.dateapp.slider.CardStackAdapter
import com.example.dateapp.utils.FirebaseAuthUtils
import com.example.dateapp.utils.FirebaseRef
import com.example.dateapp.utils.MyInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
                    Log.d(TAG, usersDataList[userCount].uid.toString())

                    userLike(uid, usersDataList[userCount].uid.toString())
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

                // 닉네임 받아오기
                MyInfo.myNickname = data?.nickname.toString()

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
        getOtherUserLikeList(otherUid)
    }

    // 사용자가 좋아요한 사람이 누구를 좋아요 했는지 확인
    private fun getOtherUserLikeList(otherUid: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 리스트에서 사용자의 UID가 있는지 확인한다.
                for (dataModel in dataSnapshot.children) {
                    val likeUserKey = dataModel.key.toString()
                    if (likeUserKey.equals(uid)) {
                        Toast.makeText(this@MainActivity, "매칭 완료", Toast.LENGTH_LONG).show()
                        createNotificationChannel()
                        sendNotification()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }

    // 알림 채널 만들기
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("test_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림 콘텐츠 생성
    private fun sendNotification(){
        var builder = NotificationCompat.Builder(this, "test_channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("매칭완료")
            .setContentText("매칭이 완료되었습니다.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)){
            if (ActivityCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }
}