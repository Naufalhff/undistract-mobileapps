package com.example.undistract.features.parental_control

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object DeviceAdminUtils {
    fun getComponentName(context: Context): ComponentName {
        return ComponentName(context, DeviceAdminReceiver::class.java)
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(getComponentName(context))
    }

    fun requestEnableDeviceAdmin(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "Aktifkan agar aplikasi tidak bisa dihapus anak-anak."
        )
        context.startActivity(intent)
    }

    fun requestDisableDeviceAdmin(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.removeActiveAdmin(getComponentName(context))
    }
}

