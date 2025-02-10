// package com.innovitegra.innova_id_verify

// import android.content.Intent
// import android.os.Bundle
// import androidx.activity.ComponentActivity
// import androidx.activity.compose.setContent
// import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Camera
// import androidx.compose.material.icons.filled.FileUpload
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Brush
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.unit.dp

// class SelectionActivity : ComponentActivity() {

//     companion object {
//         private const val TAG = "SelectionActivity"
//         private var activityClosedCallback: (() -> Unit)? = null

//         fun setActivityClosedCallback(callback: () -> Unit) {
//             activityClosedCallback = callback
//         }
//     }

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         val referenceNum = intent.getStringExtra("REFERENCE_NUMBER")

//         setContent {
//             MaterialTheme {
//                 Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
//                     Box(
//                             modifier =
//                                     Modifier.fillMaxSize()
//                                             .background(
//                                                     brush =
//                                                             Brush.verticalGradient(
//                                                                 colors = listOf(
//                                                                     Color(0xFFD1F1FF),
//                                                                     Color(0xFFFFFFFF)
//                                                             ),
//                                                             )
//                                             )
//                     ) {
//                         Column(
//                                 modifier = Modifier.fillMaxSize().padding(16.dp),
//                                 horizontalAlignment = Alignment.CenterHorizontally,
//                                 verticalArrangement = Arrangement.Bottom
//                         ) {
//                             // Heading and subtitle at the top

//                             Spacer(modifier = Modifier.height(30.dp))
//                             Text(
//                                 text = "Select ID Card Type",
//                                 color = Color.Black,
//                                 style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
//                                 modifier = Modifier.padding(bottom = 8.dp)
//                             )
//                             Text(
//                                 text = "Choose how you want to proceed with your eKYC verification",
//                                 color = Color.Gray,
//                                 style = MaterialTheme.typography.bodyLarge,
//                                 modifier = Modifier.padding(bottom = 32.dp),
//                                 textAlign = androidx.compose.ui.text.style.TextAlign.Center
//                             )

//                             // Spacer that will push content to bottom
//                             Spacer(modifier = Modifier.weight(1f))

//                             // Physical ID Card
//                             Card(
//                                     modifier =
//                                             Modifier.fillMaxWidth()
//                                                     .height(195.dp)
//                                                     .padding(horizontal = 16.dp)
//                                                     .clickable {
//                                                         val intent =
//                                                                 Intent(
//                                                                         this@SelectionActivity,
//                                                                         Inno::class
//                                                                                 .java
//                                                                 )
//                                                         intent.putExtra(
//                                                                 "REFERENCE_NUMBER",
//                                                                 referenceNum
//                                                         )
//                                                         startActivity(intent)
//                                                         finish()
//                                                     },
//                                     shape = RoundedCornerShape(12.dp),
//                                     colors =
//                                             CardDefaults.cardColors(
//                                                 containerColor = Color(0xFF221BC7)
//                                             )
//                             ) {
//                                 Column(
//                                         modifier = Modifier.fillMaxSize(),
//                                         horizontalAlignment = Alignment.CenterHorizontally,
//                                         verticalArrangement = Arrangement.Center
//                                 ) {
//                                     Icon(
//                                             imageVector = Icons.Filled.Camera,
//                                             contentDescription = "Camera Icon",
//                                             tint = Color.White,
//                                             modifier = Modifier.size(48.dp)
//                                     )
//                                     Spacer(modifier = Modifier.height(16.dp))
//                                     Text(
//                                             text = "Physical ID",
//                                             color = Color.White,
//                                             style = MaterialTheme.typography.headlineMedium
//                                     )
//                                     Text(
//                                             text = "Capture physical card image",
//                                             color = Color.White.copy(alpha = 0.8f),
//                                             style = MaterialTheme.typography.bodyLarge,
//                                             modifier = Modifier.padding(top = 4.dp)
//                                     )
//                                 }
//                             }


//                             Spacer(modifier = Modifier.height(30.dp))
//                             Card(
//                                     modifier =
//                                             Modifier.fillMaxWidth()
//                                                     .height(195.dp)
//                                                     .padding(horizontal = 16.dp)
//                                                     .clickable {
//                                                         val intent =
//                                                                 Intent(
//                                                                         this@SelectionActivity,
//                                                                         DigitalIDFrontActivity::class
//                                                                                 .java
//                                                                 )
//                                                         intent.putExtra(
//                                                                 "REFERENCE_NUMBER",
//                                                                 referenceNum
//                                                         )
//                                                         startActivity(intent)
//                                                         finish()
//                                                     },
//                                     shape = RoundedCornerShape(12.dp),
//                                     colors =
//                                             CardDefaults.cardColors(
//                                                     containerColor = Color(0xFF221BC7)
//                                             )
//                             ) {
//                                 Column(
//                                         modifier = Modifier.fillMaxSize(),
//                                         horizontalAlignment = Alignment.CenterHorizontally,
//                                         verticalArrangement = Arrangement.Center
//                                 ) {
//                                     Icon(
//                                             imageVector = Icons.Default.FileUpload,
//                                             contentDescription = "Upload Icon",
//                                             tint = Color.White,
//                                             modifier = Modifier.size(48.dp)
//                                     )
//                                     Spacer(modifier = Modifier.height(16.dp))
//                                     Text(
//                                             text = "Digital ID",
//                                             color = Color.White,
//                                             style = MaterialTheme.typography.headlineMedium
//                                     )
//                                     Text(
//                                             text = "Upload digital card image",
//                                             color = Color.White.copy(alpha = 0.8f),
//                                             style = MaterialTheme.typography.bodyLarge,
//                                             modifier = Modifier.padding(top = 4.dp)
//                                     )
//                                 }
//                             }
//                             Spacer(modifier = Modifier.height(30.dp))
//                         }
//                     }
//                 }
//             }
//         }
//     }
//     override fun onBackPressed() {
//         super.onBackPressed()
//         activityClosedCallback?.invoke()
//         finish()
//     }
// }
