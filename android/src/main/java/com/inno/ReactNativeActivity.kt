package com.inno

import android.os.Bundle
import android.util.Base64
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.ReactRootView
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

class ReactNativeActivity : ReactActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }

    override fun getMainComponentName(): String = "Verification"

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun getLaunchOptions(): Bundle? {
                try {
                    // Get all data from intent
                    val selfieBytes = intent.getByteArrayExtra("selfieImage")
                    val frontBytes = intent.getByteArrayExtra("frontImage")
                    val backBytes = intent.getByteArrayExtra("backImage")
                    val frontOcrData = intent.getSerializableExtra("frontOcr") as? OcrResponseFront
                    val backOcrData = intent.getSerializableExtra("backOcr") as? OcrResponseBack

                    // Create props bundle
                    val props = Bundle()

                    // Convert images to base64 and add to props
                    selfieBytes?.let {
                        props.putString("selfieImage", Base64.encodeToString(it, Base64.DEFAULT))
                    }
                    frontBytes?.let {
                        props.putString("frontImage", Base64.encodeToString(it, Base64.DEFAULT))
                    }
                    backBytes?.let {
                        props.putString("backImage", Base64.encodeToString(it, Base64.DEFAULT))
                    }

                    // Add OCR data
                    frontOcrData?.let {
                        val frontOcrBundle = Bundle()
                        frontOcrBundle.putString("fullName", it.fullName)
                        frontOcrBundle.putString("dateOfBirth", it.dateOfBirth)
                        frontOcrBundle.putString("sex", it.sex)
                        frontOcrBundle.putString("nationality", it.nationality)
                        props.putBundle("frontOcrData", frontOcrBundle)
                    }

                    backOcrData?.let {
                        val backOcrBundle = Bundle()
                        backOcrBundle.putString("FIN", it.FIN)
                        backOcrBundle.putString("dateOfExpiry", it.Date_of_Expiry)
                        backOcrBundle.putString("phoneNumber", it.Phone_Number)
                        props.putBundle("backOcrData", backOcrBundle)
                    }

                    // Create initial props bundle
                    val initialProps = Bundle()
                    initialProps.putBundle("initialProps", props)

                    Log.d("ReactNativeActivity", "Launch options created: $initialProps")
                    return initialProps

                } catch (e: Exception) {
                    Log.e("ReactNativeActivity", "Error creating launch options", e)
                    return null
                }
            }
        }
    }
}
