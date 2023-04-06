package com.example.dateapp.message.fcm

import com.example.dateapp.message.fcm.Repo.Companion.CONTENT_TYPE
import com.example.dateapp.message.fcm.Repo.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Firebase Server 로 메세지 보내라고 명령
interface NotificationApi {
    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>
}