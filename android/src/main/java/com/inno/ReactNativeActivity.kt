// // package com.inno

// // import android.os.Bundle
// // import android.util.Log
// // import com.facebook.react.ReactActivity
// // import com.facebook.react.ReactActivityDelegate
// // import android.content.Context
// // import org.json.JSONObject
// // import java.io.File

// // class ReactNativeActivity : ReactActivity() {
    

// //     override fun onCreate(savedInstanceState: Bundle?) {
// //         super.onCreate(savedInstanceState)
// //         Log.d("ReactNativeActivity", "onCreate called")
// //     }

// //     override fun getMainComponentName(): String = "InnoExample"

    

    
// //     override fun createReactActivityDelegate(): ReactActivityDelegate {
// //         Log.d("ReactNativeActivity", "createReactActivityDelegate called")
// //         return object : ReactActivityDelegate(this, mainComponentName) {
// //             override fun getLaunchOptions(): Bundle? {
// //                 Log.d("ReactNativeActivity", "getLaunchOptions called")
// //                 return try {
// //                     // Get data as String from Intent
// //                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0 ) ?: ""
// //                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
// //                     val verificationStatus = intent.getStringExtra("verificationStatus") ?: ""
// //                     Log.d("ReactNativeActivity", "Session timeout status: $sessionTimeoutStatus")
// //                     Log.d("ReactNativeActivity", "Reference number: $referenceNumber")
// //                     Log.d("ReactNativeActivity", "Verification status: $verificationStatus")

// //                     // Create props bundle
// //                     val props = Bundle()
// //                     props.putString("referenceNumber", referenceNumber)
// //                     props.putString("verificationStatus", verificationStatus)
// //                     props.putInt("sessionTimeoutStatus", sessionTimeoutStatus as? Int ?: 0)


// //                     // Create initial props bundle
// //                     val initialProps = Bundle()
// //                     initialProps.putBundle("initialProps", props)

// //                     Log.d("ReactNativeActivity", "Launch options created: $initialProps")
// //                     initialProps

// //                 } catch (e: Exception) {
// //                     Log.e("ReactNativeActivity", "Error creating launch options", e)
// //                     null
// //                 }
// //             }
// //         }
// //     }
// // }



// package com.inno

// import android.os.Bundle
// import android.util.Log
// import com.facebook.react.ReactActivity
// import com.facebook.react.ReactActivityDelegate

// class ReactNativeActivity : ReactActivity() {
    
//      private var componentName: String = ""


//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//     }
    

//     override fun getMainComponentName(): String {
//          componentName = intent?.getStringExtra("apkName") ?: ""
//         Log.d("ReactNativeActivityData", "Main component name getmainComponenet : $componentName")
//         val componentName = GlobalDataHolder.getData()
//         return componentName ?: ""
// }

//     override fun createReactActivityDelegate(): ReactActivityDelegate {
//         Log.d("ReactNativeActivityData", "createReactActivityDelegate called")
//         return object : ReactActivityDelegate(this, mainComponentName) {
//             override fun getLaunchOptions(): Bundle? {
//                 Log.d("ReactNativeActivityData", "getLaunchOptions called")
//                 return try {
//                     // Get data as String from Intent
//                     val sessionTimeoutStatus = intent.getIntExtra("sessionTimeoutStatus", 0)
//                     val referenceNumber = intent.getStringExtra("referenceNumber") ?: ""
//                     val verificationStatus = intent.getIntExtra("verificationStatus", 0)
//                     val apkName = intent.getStringExtra("apkName") ?: ""
//                     Log.d("ReactNativeActivityData", "Session timeout status: $sessionTimeoutStatus")
//                     Log.d("ReactNativeActivityData", "Reference number: $referenceNumber")
//                     Log.d("ReactNativeActivityData", "Verification status: $verificationStatus")
//                     Log.d("ReactNativeActivityData", "Apk name: $apkName")

//                     // Create props bundle
//                     val props = Bundle()
//                     props.putString("referenceNumber", referenceNumber)
//                     props.putInt("verificationStatus", verificationStatus)
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