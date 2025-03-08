package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import android.content.Context
import org.json.JSONObject
import java.io.File

class ReactNativeActivity : ReactActivity() {

    // override fun onCreate(savedInstanceState: Bundle?) {
    //     super.onCreate(savedInstanceState)
    //     Log.d("ReactNativeActivity", "onCreate called")
    // }

    // override fun getMainComponentName(): String = "InnoExample"

    private fun getAppNameFromJson(): String {
        return try {
            val inputStream = assets.open("app.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            jsonObject.getString("name")
        } catch (e: Exception) {
            Log.e("ReactNativeActivity", "Error reading app.json", e)
            "InnoExample" // fallback default name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }

    override fun getMainComponentName(): String = "InnoTrustAndroid"

    
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