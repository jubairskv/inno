package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate


class ReactNativeActivity : ReactActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }
    

     override fun getMainComponentName(): String = "InnoExample"

//     override fun getMainComponentName(): String {
//     val packageManager = packageManager
//     val componentName = try {
//         val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
//         applicationInfo.name ?: applicationInfo.className ?: packageName // Use name or className if available
//     } catch (e: PackageManager.NameNotFoundException) {
//         packageName // Fallback to package name if app info is unavailable
//     }

//     val intent = packageManager.getLaunchIntentForPackage(packageName)
//     intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//     startActivity(intent)

//     return componentName
// }


    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun getLaunchOptions(): Bundle? {
                return try {
                    // Get data as String from Intent
                    val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
                    val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""

                    // Create props bundle
                    val props = Bundle()
                    props.putString("referenceNumber", referenceNumber)
                    props.putString("verificationStatus", verificationStatus)

                    // Create initial props bundle
                    val initialProps = Bundle()
                    initialProps.putBundle("initialProps", props)

                    Log.d("ReactNativeActivitys", "Launch options created: $initialProps")
                    initialProps

                } catch (e: Exception) {
                    Log.e("ReactNativeActivitys", "Error creating launch options", e)
                    null
                }
            }
        }
    }
}
