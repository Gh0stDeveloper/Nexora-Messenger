package com.nexora.app.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RelayApi {
    @POST("/v1/relay/users/me/bootstrap")
    suspend fun bootstrapProfile(@Body body: BootstrapProfileRequest): BootstrapProfileResponse

    @POST("/v1/relay/users/me/fcm-token")
    suspend fun syncFcmToken(@Body body: FcmTokenRequest): ApiEnvelope

    @Multipart
    @POST("/v1/relay/users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): AvatarUploadResponse

    @GET("/v1/relay/chats")
    suspend fun getChats(@Query("limit") limit: Int = 100): ChatsResponse

    @GET("/v1/relay/chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("since") since: Long = 0,
        @Query("limit") limit: Int = 200,
    ): MessagesResponse

    @POST("/v1/relay/messages")
    suspend fun sendMessage(@Body body: SendMessageRequest): SendMessageResponse
}
