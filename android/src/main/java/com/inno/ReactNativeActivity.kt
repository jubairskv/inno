package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.ReactApplication

class ReactNativeActivity : ReactActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
        
        // Get data from intent
        val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
        val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
        
        // Emit event with verification data
        emitVerificationEvent(referenceNumber, verificationStatus)
    }

    private fun emitVerificationEvent(referenceNumber: String, verificationStatus: String) {
        try {
            val params = Arguments.createMap().apply {
                putString("referenceNumber", referenceNumber)
                putString("verificationStatus", verificationStatus)
            }

            // Get ReactContext and emit event
            (application as ReactApplication)
                .reactNativeHost
                .reactInstanceManager
                .currentReactContext
                ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onReferenceIDReceived", params)

            Log.d("ReactNativeActivity", "Event emitted: $referenceNumber, $verificationStatus")
        } catch (e: Exception) {
            Log.e("ReactNativeActivity", "Error emitting event: ${e.message}")
        }
    }

    override fun getMainComponentName(): String = "Verification"

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun getLaunchOptions(): Bundle? {
                return Bundle().apply {
                    putBundle("initialProps", Bundle().apply {
                        putBoolean("isFromNative", true)
                    })
                }
            }
        }
    }
}
