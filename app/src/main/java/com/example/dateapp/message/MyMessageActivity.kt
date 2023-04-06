package com.example.dateapp.message

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.dateapp.R
import com.example.dateapp.utils.FirebaseAuthUtils
import com.example.dateapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MyMessageActivity : AppCompatActivity() {

    private val TAG = MyMessageActivity::class.java.simpleName

    lateinit var listviewAdapter: MessageAdapter
    val messageList = mutableListOf<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_message)

        val listview = findViewById<ListView>(R.id.messageListView)

        listviewAdapter = MessageAdapter(this, messageList)
        listview.adapter = listviewAdapter

        getMyMessage()
    }

    private fun getMyMessage() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                messageList.clear()

                for (dataModel in dataSnapshot.children) {
                    val message = dataModel.getValue(MessageModel::class.java)
                    messageList.add(message!!)
                    Log.d(TAG, message.toString())
                }
                messageList.reverse()
                listviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCanceled", databaseError.toException())
            }
        }
        FirebaseRef.userMessageRef.child(FirebaseAuthUtils.getUid())
            .addValueEventListener(postListener)
    }
}