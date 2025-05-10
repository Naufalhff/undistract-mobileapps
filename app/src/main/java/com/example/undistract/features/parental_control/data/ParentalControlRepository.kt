package com.example.undistract.features.parental_control.data

import com.example.undistract.features.parental_control.data.local.PinDao
import com.example.undistract.features.parental_control.data.local.PinEntity

class ParentalControlRepository (
    private val pinDao: PinDao
) {

    suspend fun addPin(
        pin: String
    ) {
        val pinEntity = PinEntity(
            pin = pin
        )
        pinDao.savePin(pinEntity)
    }

    suspend fun getPin(): String? {
        return pinDao.getPin()
    }
}