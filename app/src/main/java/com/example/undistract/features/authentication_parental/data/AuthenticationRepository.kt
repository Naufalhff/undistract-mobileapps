package com.example.undistract.features.authentication_parental.data

import com.example.undistract.core.ApiService
import com.example.undistract.core.SendOtpRequest
import com.example.undistract.core.VerifyOtpRequest
import com.example.undistract.features.authentication_parental.data.local.PinDao
import com.example.undistract.features.authentication_parental.data.local.PinEntity
import retrofit2.HttpException
import java.io.IOException

class AuthenticationRepository(
    private val api: ApiService,
    private val pinDao: PinDao
) {
    suspend fun sendOtp(email: String): Boolean {
        return try {
            val response = api.sendOtp(SendOtpRequest(email))
            response.isSuccessful && response.body()?.success == true
        } catch (e: IOException) {
            false
        } catch (e: HttpException) {
            false
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Boolean {
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(email, otp))
            response.isSuccessful && response.body()?.success == true
        } catch (e: IOException) {
            false
        } catch (e: HttpException) {
            false
        }
    }

    suspend fun addPin(
        pin: String,
        email: String
    ) {
        val pinEntity = PinEntity(
            pin = pin,
            email = email,
            isSynced = false
        )
        pinDao.savePin(pinEntity)
    }

    suspend fun getPin(): String? {
        return pinDao.getPin()
    }

    suspend fun getEmail(): String? {
        return pinDao.getEmail()
    }

    suspend fun updatePin(newPin: String) {
        pinDao.updatePin(newPin)
    }
}
