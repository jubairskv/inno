package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import android.content.Context
import org.json.JSONObject
import java.io.File

class ReactNativeActivity : ReactActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }

    override fun getMainComponentName(): String = "InnoExample"

    // override fun getMainComponentName(): String {
    //     Log.d("ReactNativeActivity", "getMainComponentName called")
    //     val appName = getAppNameFromJson(this) ?: packageName.substringAfterLast('.')
    //     Log.d("ReactNativeActivity", "App name: $appName")
    //     return appName
    // }

    // fun getAppNameFromJson(context: Context): String? {
    //     Log.d("ReactNativeActivity", "getAppNameFromJson called")
    //     return try {
    //         // Get the app.json file from the root level
    //         val file = File(context.filesDir.parentFile, "app.json") // Adjust the path as needed
    //         Log.d("ReactNativeActivity", "File path: ${file.absolutePath}")
    //         if (file.exists()) {
    //             Log.d("ReactNativeActivity", "app.json file exists")
    //             // Explicitly use File.readText() to avoid ambiguity
    //             val jsonString = file.readText(Charsets.UTF_8) // Specify charset explicitly
    //             Log.d("ReactNativeActivity", "JSON string: $jsonString")
    //             val jsonObject = JSONObject(jsonString) // Convert to JSON object
    //             val appName = jsonObject.optString("name", null) // Get "name" value from JSON
    //             Log.d("ReactNativeActivity", "App name from JSON: $appName")
    //             appName
    //         } else {
    //             Log.d("ReactNativeActivity", "app.json file does not exist")
    //             null // Return null if the file doesn't exist
    //         }
    //     } catch (e: Exception) {
    //         Log.e("ReactNativeActivity", "Error reading app.json", e)
    //         null // Return null if any error occurs
    //     }
    // }

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        Log.d("ReactNativeActivity", "createReactActivityDelegate called")
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun getLaunchOptions(): Bundle? {
                Log.d("ReactNativeActivity", "getLaunchOptions called")
                return try {
                    // Get data as String from Intent
                    val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
                    val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
                    Log.d("ReactNativeActivity", "Reference number: $referenceNumber")
                    Log.d("ReactNativeActivity", "Verification status: $verificationStatus")

                    // Create props bundle
                    val props = Bundle()
                    props.putString("referenceNumber", referenceNumber)
                    props.putString("verificationStatus", verificationStatus)

                    // Create initial props bundle
                    val initialProps = Bundle()
                    initialProps.putBundle("initialProps", props)

                    Log.d("ReactNativeActivity", "Launch options created: $initialProps")
                    initialProps

                } catch (e: Exception) {
                    Log.e("ReactNativeActivity", "Error creating launch options", e)
                    null
                }
            }
        }
    }
}