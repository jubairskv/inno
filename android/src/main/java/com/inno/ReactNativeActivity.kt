package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
// import android.content.Context
// import org.json.JSONObject
// import java.io.InputStream


class ReactNativeActivity : ReactActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }
    

     override fun getMainComponentName(): String = "InnoExample"

    // override fun getMainComponentName(): String {
    //     return getAppNameFromJson(this) ?: packageName.substringAfterLast('.')
    // }

    // fun getAppNameFromJson(context: Context): String? {
    //     return try {
    //         val inputStream: InputStream = context.assets.open("app.json") // Load app.json from assets
    //         val jsonString = inputStream.bufferedReader().use { it.readText() } // Read JSON as text
    //         val jsonObject = JSONObject(jsonString) // Convert to JSON object
    //         jsonObject.optString("name", null) // Get "name" value from JSON
    //     } catch (e: Exception) {
    //         e.printStackTrace()
    //         null // Return null if any error occurs
    //     }
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
