// package com.inno

// import android.util.Log
// import com.facebook.react.bridge.ReactApplicationContext
// import com.facebook.react.bridge.ReactContextBaseJavaModule
// import com.facebook.react.bridge.ReactMethod
// import com.facebook.react.bridge.Promise
// import com.facebook.react.bridge.WritableNativeMap

// class DataModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

//     override fun getName(): String {
//         return "DataModule"
//     }

//     // Method to pass data to React Native
//     @ReactMethod
//     fun getInitialData(promise: Promise) {
//         val currentActivity = currentActivity
//         if (currentActivity is ReactNativeActivity) {
//             val intent = currentActivity.intent
//             val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//             val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//             val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
//             val apkName = intent.getStringExtra("apkName") ?: ""

//             val data = WritableNativeMap().apply {
//                 putString("referenceNumber", referenceNumber)
//                 putString("verificationStatus", verificationStatus)
//                 putInt("sessionTimeoutStatus", sessionTimeoutStatus)
//                 putString("apkName", apkName)
//             }

//             promise.resolve(data)
//         } else {
//             promise.reject("NO_ACTIVITY", "Current activity is not ReactNativeActivity")
//         }
//     }
// }