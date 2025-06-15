package com.example.undistract.core

import android.content.Context
import com.example.undistract.features.authentication_parental.data.local.PinEntity
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// OTP Data
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

// Sync Data
data class SyncRequestBody(
    val blockSchedules: List<BlockSchedulesEntity>,
    val variableSessions: List<VariableSessionEntity>,
    val blockPermanents: List<BlockPermanentEntity>,
    val dailyLimits: List<SetaDailyLimitEntity>,
    val userParents: List<PinEntity>,
    val existingUuids: ExistingUuids
)
data class SyncResponse(
    val blockSchedules: List<BlockSchedulesEntity>,
    val variableSessions: List<VariableSessionEntity>,
    val blockPermanents: List<BlockPermanentEntity>,
    val dailyLimits: List<SetaDailyLimitEntity>,
    val userParents: List<PinEntity>
)
data class ExistingUuids(
    val blockSchedules: List<String>,
    val variableSessions: List<String>,
    val blockPermanents: List<String>,
    val dailyLimits: List<String>
)

// Login Data
data class LoginRequest(
    val email: String,
    val password: String
)
data class User(
    val id: Int,
    val name: String,
    val email: String
)
data class UserWrapper(
    val user: User
)
data class LoginResponse(
    val status: String,
    val data: UserWrapper,
    val token: String,
    val message: String
)

// Register Data
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

interface ApiService {
    // OTP Endpoint
    @POST("/send_otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>
    @POST("/verify_otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    // Sync Endpoint
    @POST("api/sync/all")
    suspend fun syncAllData(@Query("userId") userId: Int, @Body body: SyncRequestBody): Response<Void>
    @GET("api/fetch/all")
    suspend fun fetchAllData(@Query("userId") userId: Int): Response<SyncResponse>


    // Login Endpoint
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // Register Endpoint
    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<Void>
}

object ApiOtpClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://undistract-otp-api-service.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

object ApiBackendClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://undistract-mobileapps-backend-production-e9bb.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

object ApiAuthClient {
    fun create(context: Context): ApiService {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://undistract-mobileapps-backend-production-e9bb.up.railway.app")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
