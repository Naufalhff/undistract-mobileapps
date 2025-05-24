package com.example.undistract.features.profile.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.undistract.core.ApiService
import com.example.undistract.core.LoginRequest
import com.example.undistract.core.RegisterRequest

class AuthRepository(private val apiService: ApiService, private val context: Context) {

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                val userId = body?.data?.user?.id
                val name = body?.data?.user?.name
                val email1 = body?.data?.user?.email
                Log.d("AUTH_RESPONSE", "Token: $token, User ID: $userId")

                if (!token.isNullOrEmpty() && userId != null) {
                    // Simpan ke SharedPreferences
                    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("token", token)
                        .putString("name", name)
                        .putString("email", email1)
                        .putInt("userId", userId)
                        .apply()
                    true
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(username: String, email: String, password: String): Boolean {
        return try {
            val registerRequest = RegisterRequest(username, email, password)
            val response = apiService.register(registerRequest)

            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    fun getAuthToken(): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    fun getLoggedInUserId(): Int {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }
}

