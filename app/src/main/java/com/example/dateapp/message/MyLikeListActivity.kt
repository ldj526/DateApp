package com.example.dateapp.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dateapp.R
import com.example.dateapp.auth.UserDataModel
import com.example.dateapp.message.fcm.NotificationModel
import com.example.dateapp.message.fcm.PushNotification
import com.example.dateapp.message.fcm.RetrofitInstance
import com.example.dateapp.utils.FirebaseAuthUtils
import com.example.dateapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext

class MyLikeListActivity : AppCompatActivity() {

    private val TAG = MyLikeListActivity::class.java.simpleName
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserListUid = mutableListOf<String>()
    private val likeUserList = mutableListOf<UserDataModel>()

    lateinit var listViewAdapter: ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)

        val userListView = findViewById<ListView>(R.id.userListView)

        listViewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listViewAdapter

        getMyLikeList()

        userListView.setOnItemClickListener { parent, view, position, id ->
            checkMatching(likeUserList[position].uid.toString())

            val notiModel = NotificationModel("a", "b")
            val pushModel = PushNotification(notiModel, likeUserList[position].token.toString())
            testPush(pushModel)
        }

        // LongClick
        userListView.setOnItemLongClickListener { parent, view, position, id ->
            checkMatching(likeUserList[position].uid.toString())
            return@setOnItemLongClickListener (true)
        }
    }

    private fun checkMatching(otherUid: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.children.count() == 0) {
                    Toast.makeText(
                        this@MyLikeListActivity,
                        "상대방이 좋아요 한 사람이 없습니다.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    for (dataModel in dataSnapshot.children) {
                        val likeUserKey = dataModel.key.toString()
                        if (likeUserKey.equals(uid)) {
                            Toast.makeText(
                                this@MyLikeListActivity,
                                "매칭이 되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            showDialog()
                        } else {

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }

    // 내가 좋아요 한 목록
    private fun getMyLikeList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataModel in dataSnapshot.children) {
                    // 좋아요 한 사람들의 uid를 likeUserListUid에 넣어준다.
                    likeUserListUid.add(dataModel.key.toString())
                }
                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)
    }

    // 전체 유저 데이터 받아오기
    private fun getUserDataList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataModel in dataSnapshot.children) {
                    val user = dataModel.getValue(UserDataModel::class.java)
                    // 전체 유저 중 사용자가 좋아요 한 사람들의 정보만 추가
                    if (likeUserListUid.contains(user?.uid)) {
                        likeUserList.add(user!!)
                    }
                }
                listViewAdapter.notifyDataSetChanged()
                Log.d(TAG, likeUserList.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }

    // Push
    private fun testPush(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }

    // Dialog
    private fun showDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메세지 보내기")

        val mAlertDialog = mBuilder.show()

        val sendBtn = mAlertDialog.findViewById<Button>(R.id.sendTextBtn)
        sendBtn?.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }
}