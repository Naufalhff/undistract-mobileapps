package com.example.undistract.features.block_permanent.domain

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.os.Handler
import android.os.Looper

class BlockPermanentManager ()
{
    fun blockApp (service: AccessibilityService) {
        service.performGlobalAction(GLOBAL_ACTION_BACK)
        Handler(Looper.getMainLooper()).postDelayed({
            service.performGlobalAction(GLOBAL_ACTION_BACK)
        }, 200)
        Handler(Looper.getMainLooper()).postDelayed({
            service.performGlobalAction(GLOBAL_ACTION_HOME)
        }, 700)
    }
}

