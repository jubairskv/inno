// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate
// import android.content.Context
// import org.json.JSONObject
// import java.io.File

// class ReactNativeActivity : ReactActivity() {
    

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         Log.d("ReactNativeActivity", "onCreate called")
//     }

//     override fun getMainComponentName(): String = "InnoExample"

    

    
//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//         Log.d("ReactNativeActivity", "createReactActivityDelegate called")
//         return object : ReactActivityDelegate(this, mainComponentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivity", "getLaunchOptions called")
//                 return try {
//                     // Get data as String from Intent
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0 ) ?: ""
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
//                     Log.d("ReactNativeActivity", "Session timeout status: $sessionTimeoutStatus")
//                     Log.d("ReactNativeActivity", "Reference number: $referenceNumber")
//                     Log.d("ReactNativeActivity", "Verification status: $verificationStatus")

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putString("verificationStatus", verificationStatus)
//                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus as? Int ?: 0)


//                     // Create initial props bundle
//                     val initialProps = Bundle()
//                     initialProps.putBundle("initialProps", props)

//                     Log.d("ReactNativeActivity", "Launch options created: $initialProps")
//                     initialProps

//                 } catch (e: Exception) {
//                     Log.e("ReactNativeActivity", "Error creating launch options", e)
//                     null
//                 }
//             }
//         }
//     }
// }



// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate

// class ReactNativeActivity : ReactActivity() {

//     // Initialize with default value
//     private var apkName: String? = "InnoExample"

//     override fun onCreate(savedInstanceState: Bundle?) {
//         // Get apkName from Intent before calling super.onCreate()
//         apkName = intent?.getStringExtra("apkName") 
//         super.onCreate(savedInstanceState)
//         Log.d("ReactNativeActivityData", "Received apkName from intent: $apkName")
//     }

//     override fun getMainComponentName(): String {
//          return apkName ?: "InnoExample"
//     }
    



//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate called with apkName: $apkName")
//         return object : ReactActivityDelegate(this, mainComponentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivityData", "getLaunchOptions called")
//                 return try {
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putString("verificationStatus", verificationStatus)
//                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus)

//                     // Create initial props bundle
//                     val initialProps = Bundle()
//                     initialProps.putBundle("initialProps", props)

//                     Log.d("ReactNativeActivityData", "Launch options created: $initialProps")
//                     initialProps

//                 } catch (e: Exception) {
//                     Log.e("ReactNativeActivityData", "Error creating launch options", e)
//                     null
//                 }
//             }
//         }
//     }
// }


// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate

// class ReactNativeActivity : ReactActivity() {

//     // Initialize with default value
//     private var apkName: String? = "Default"

//     override fun onCreate(savedInstanceState: Bundle?) {
//         // Get apkName from Intent before calling super.onCreate()
//         apkName = intent?.getStringExtra("apkName") ?: "Default"
//         super.onCreate(savedInstanceState)
//         Log.d("ReactNativeActivityData", "Received apkName from intent: $apkName")
//     }

//     override fun getMainComponentName(): String {
//         return apkName ?: "Default"
//     }

//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//          apkName = intent?.getStringExtra("apkName") ?: "Default"
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate called with apkName: $componentName")
//         return object : ReactActivityDelegate(this, mainComponentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivityData", "getLaunchOptions called")
//                 return try {
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putString("verificationStatus", verificationStatus)
//                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus)

//                     // Create initial props bundle
//                     val initialProps = Bundle()
//                     initialProps.putBundle("initialProps", props)

//                     Log.d("ReactNativeActivityData", "Launch options created: $initialProps")
//                     initialProps

//                 } catch (e: Exception) {
//                     Log.e("ReactNativeActivityData", "Error creating launch options", e)
//                     null
//                 }
//             }
//         }
//     }
// }

// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate

// class ReactNativeActivity : ReactActivity() {

//     // Declare the global variable at the class level
//     private var apkName: String? = null

//     override fun onCreate(savedInstanceState: Bundle?) {
//         Log.d("ReactNativeActivityData", "onCreate started")

//         // Assign the value to the global variable inside onCreate()
//         apkName = intent?.getStringExtra("apkName")
//         Log.d("ReactNativeActivityData", "Received apkName from intent: $apkName")

//         // Call super.onCreate() after initializing apkName
//         super.onCreate(savedInstanceState)

//         Log.d("ReactNativeActivityData", "onCreate completed")
//     }

//     override fun getMainComponentName(): String {
//         Log.d("ReactNativeActivityData", "getMainComponentName called")
//         return apkName ?: "Default"
//     }

//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate started")

//         // Use the global apkName value
//         val componentName = apkName ?: "Default"
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate called with apkName: $componentName")

//         return object : ReactActivityDelegate(this, componentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivityData", "getLaunchOptions called")
//                 return try {
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putString("verificationStatus", verificationStatus)
//                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus)

//                     // Create initial props bundle
//                     val initialProps = Bundle()
//                     initialProps.putBundle("initialProps", props)

//                     Log.d("ReactNativeActivityData", "Launch options created: $initialProps")
//                     initialProps

//                 } catch (e: Exception) {
//                     Log.e("ReactNativeActivityData", "Error creating launch options", e)
//                     null
//                 }
//             }
//         }
//     }
// }

// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate
// import androidx.activity.viewModels
// import androidx.lifecycle.lifecycleScope
// import kotlinx.coroutines.flow.collect
// import kotlinx.coroutines.launch

// class ReactNativeActivity : ReactActivity() {

//     private val sharedViewModel: SharedViewModel by viewModels()

//     override fun onCreate(savedInstanceState: Bundle?) {
//         Log.d("ReactNativeActivityData", "1- onCreate started")
//         super.onCreate(savedInstanceState)
//         Log.d("ReactNativeActivityData", "onCreate completed")
//     }

//     override fun getMainComponentName(): String {
//         Log.d("ReactNativeActivityData", "getMainComponentName called -2")

//         // Retrieve apkName directly from the intent
//         val apkName = intent?.getStringExtra("apkName") ?: "Default"
//         Log.d("ReactNativeActivityData", "Retrieved apkName from intent: $apkName")

//         return apkName
//     }

//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate started -3")

//         // Use the value returned by getMainComponentName() as the default
//         var componentName = mainComponentName // Default value (non-nullable String)

//         // Access the current value of apkName from sharedViewModel
//         val apkName = sharedViewModel.apkName.value
//         componentName = apkName ?: "Default"
//         Log.d("ReactNativeActivityData", "Updated componentName with apkName: $componentName")

//         Log.d("ReactNativeActivityData", "createReactActivityDelegate called with apkName: $componentName")

//         return object : ReactActivityDelegate(this, componentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivityData", "getLaunchOptions called")
//                 return try {
//                     // Retrieve data from the intent
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putString("verificationStatus", verificationStatus)
//                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus)

//                     // Create initial props bundle
//                     val initialProps = Bundle()
//                     initialProps.putBundle("initialProps", props)

//                     Log.d("ReactNativeActivityData", "Launch options created: $initialProps")
//                     initialProps

//                 } catch (e: Exception) {
//                     Log.e("ReactNativeActivityData", "Error creating launch options", e)
//                     null
//                 }
//             }
//         }
//     }
// }


package com.inno

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate

class ReactNativeActivity : ReactActivity() {
    
     private var componentName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
         componentName = intent?.getStringExtra("apkName") ?: ""
        Log.d("ReactNativeActivityData", "Main component name onCreate: $componentName")
        super.onCreate(savedInstanceState)
        Log.d("ReactNativeActivity", "onCreate called")
    }

    override fun getMainComponentName(): String {
         componentName = intent?.getStringExtra("apkName") ?: ""
        Log.d("ReactNativeActivityData", "Main component name getmainComponenet : $componentName")
        return componentName 
    }

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        Log.d("ReactNativeActivityData", "createReactActivityDelegate called")
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun getLaunchOptions(): Bundle? {
                Log.d("ReactNativeActivityData", "getLaunchOptions called")
                return try {
                    // Get data as String from Intent
                    val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
                    val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
                    val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
                    val apkName = intent.getStringExtra("apkName") ?: ""
                    Log.d("ReactNativeActivityData", "Session timeout status: $sessionTimeoutStatus")
                    Log.d("ReactNativeActivityData", "Reference number: $referenceNumber")
                    Log.d("ReactNativeActivityData", "Verification status: $verificationStatus")
                    Log.d("ReactNativeActivityData", "Apk name: $apkName")

                    // Create props bundle
                    val props = Bundle()
                    props.putString("referenceNumber", referenceNumber)
                    props.putString("verificationStatus", verificationStatus)
                    props.putInt("sessionTimeoutStatus", sessionTimeoutStatus)

                    // Create initial props bundle
                    val initialProps = Bundle()
                    initialProps.putBundle("initialProps", props)

                    Log.d("ReactNativeActivityData", "Launch options created: $initialProps")
                    initialProps
                } catch (e: Exception) {
                    Log.e("ReactNativeActivityData", "Error creating launch options", e)
                    null
                }
            }
        }
    }
}