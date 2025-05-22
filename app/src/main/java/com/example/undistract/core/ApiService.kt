package com.example.undistract.core

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val success: Boolean,
    val otp: Int?,
    val message: String?
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String?
)

interface ApiService {
    @POST("/send_otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    @POST("/verify_otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>
}

object ApiClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://responsible-reprieve-production.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}