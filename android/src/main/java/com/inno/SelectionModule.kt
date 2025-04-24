package com.inno

import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*

class SelectionModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val TAG = "SelectionModule"
       // var resultCallback: Callback? = null
    }

    override fun getName() = "SelectionModule"

    @ReactMethod
    fun openSelectionScreen(referenceNumber: String, apkName: String, promise: Promise) {
        try {
            Log.d(TAG, "Opening selection screen with reference: $referenceNumber, apkName: $apkName")
            val intent = Intent(reactContext, SelectionActivity::class.java)
            intent.putExtra("REFERENCE_NUMBER", referenceNumber)
            intent.putExtra("APK_NAME", apkName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            reactContext.startActivity(intent)
            Log.d(TAG, "Selection screen launched successfully")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open selection screen: ${e.message}")
            promise.reject("ERROR", "Failed to open selection screen: ${e.message}")
        }
    }
}