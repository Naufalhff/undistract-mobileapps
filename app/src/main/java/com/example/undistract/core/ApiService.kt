package com.example.undistract.core

import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity
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

data class SyncRequestBody(
    val blockSchedules: List<BlockSchedulesEntity>,
    val variableSessions: List<VariableSessionEntity>,
    val blockPermanents: List<BlockPermanentEntity>,
    val dailyLimits: List<SetaDailyLimitEntity>
)

interface ApiService {
    @POST("/send_otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    @POST("/verify_otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/sync/all")
    suspend fun syncAllData(@Body body: SyncRequestBody): Response<Void>
}

object ApiOtpClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://responsible-reprieve-production.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

object ApiBackendClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://undistract-mobileapps-backend-production.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}