package com.inno

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import android.util.Log
import androidx.camera.view.PreviewView
import android.widget.FrameLayout
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.ViewGroup
import android.widget.Button
import android.view.Gravity
import android.widget.TextView
import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.ProgressBar
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Serializable
import android.app.AlertDialog
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import android.graphics.BitmapFactory
import android.graphics.Matrix
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.content.res.ColorStateList
import android.widget.ScrollView
import android.widget.ImageView
import java.net.URL
import androidx.lifecycle.lifecycleScope
import java.io.File
import android.widget.Toast
import android.graphics.Typeface
import android.view.SurfaceView
import android.view.SurfaceHolder
import java.io.IOException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import android.view.TextureView
import com.google.common.util.concurrent.ListenableFuture
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.camera.core.*
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import android.graphics.Canvas
import android.graphics.Paint
import android.annotation.SuppressLint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.graphics.Rect
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.RelativeLayout
import android.content.Context
import android.view.Surface
import androidx.activity.result.contract.ActivityResultContracts
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.content.pm.ActivityInfo
import android.media.MediaActionSound
import android.util.Base64
import androidx.lifecycle.Observer
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.cancel
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.ReactNativeHost
import com.facebook.react.bridge.ReactApplicationContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.ReactInstanceEventListener
import com.facebook.react.ReactInstanceManager


abstract class BaseTimeoutActivity : AppCompatActivity() {
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val TIMEOUT_DURATION = 10000L // 3 minutes
    private var reactContext: ReactContext? = null

    private val timeoutRunnable = Runnable {
        cleanupAndReturnToLaunch(1) // Pass 1 to indicate timeout
    }

    protected abstract fun cleanupResources()

    private fun initializeReactContext() {
        try {
            val app = application as? ReactApplication
            if (app != null) {
                val reactInstanceManager = app.reactNativeHost.reactInstanceManager
                // If there's no current React context, create one
                if (reactInstanceManager.currentReactContext == null) {
                    Log.d("BaseTimeoutActivity", "Creating new React context")
                    reactInstanceManager.createReactContextInBackground()
                }
                // Add listener for when React context is created/changed
                reactInstanceManager.addReactInstanceEventListener(object : ReactInstanceEventListener {
                    override fun onReactContextInitialized(context: ReactContext) {
                        Log.d("BaseTimeoutActivity", "React context created/ changed")
                        reactContext = context
                    }
                })
            } else {
                Log.e("BaseTimeoutActivity", "Application is not a ReactApplication")
            }
        } catch (e: Exception) {
            Log.e("BaseTimeoutActivity", "Error initializing React context: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cleanupAndReturnToLaunch(timeoutStatus: Int) {
        try {
            val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
            sharedViewModel.apply {
                setSessionTimeout(1 , "Session timed out. Please try again.")
                updateSessionTimeout(true)
                clearAllData()
            }

            val sessionOut =

            cleanupResources()

            // Emit timeout event
            val message = if (timeoutStatus == 1) "Session timed out. Please try again." else null
            Log.d("BaseTimeoutActivity", "Emitting timeout event with status: $timeoutStatus, message: $message")
            emitTimeoutEvent(timeoutStatus, message)

            // Navigate back to main screen
            // val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            //     flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // }
            // startActivity(intent)
            // finish()

            Handler(Looper.getMainLooper()).postDelayed({
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }, 500) // 500ms delay
        } catch (e: Exception) {
            Log.e("BaseTimeoutActivity", "Error during cleanup: ${e.message}")
            finish()
        }
    }

    // private fun cleanupAndReturnToLaunch(timeoutStatus: Int) {
    //     try {
    //         val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
    //          sharedViewModel.apply {
                    
    //                 updateSessionTimeout(true)
                    
    //             }

    //         val message = if (timeoutStatus == 1) "Session timed out. Please try again." else null

    //         // Emit timeout event before clearing data
    //         Log.d("BaseTimeoutActivity", "Emitting timeout event before clearing data: status=$timeoutStatus, message=$message")
    //         emitTimeoutEvent(timeoutStatus, message)

    //         // Delay for a short time to ensure React Native receives the event
    //         Handler(Looper.getMainLooper()).postDelayed({
    //             // Update ViewModel and clear data
    //             sharedViewModel.apply {
    //                 setSessionTimeout(timeoutStatus, message)
    //                 clearAllData()
    //                 updateSessionTimeout(true)
                
    //             }

    //             cleanupResources()

    //             // Navigate back to main screen
    //             val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
    //                 flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    //             }
    //             startActivity(intent)
    //             finish()
    //         }, 1000) // Adjust delay if needed
    //     } catch (e: Exception) {
    //         Log.e("BaseTimeoutActivity", "Error during cleanup: ${e.message}")
    //         finish()
    //     }
    // }



    private fun emitTimeoutEvent(status: Int, message: String?) {
        try {
            val reactContext = getReactContext()
            if (reactContext == null) {
                Log.e("BaseTimeoutActivity", "ReactContext is null")
                return
            }

            // Get the TimeoutEventModule instance
            val timeoutModule = reactContext
                .getNativeModule(TimeoutEventModule::class.java)

            if (timeoutModule == null) {
                Log.e("BaseTimeoutActivity", "TimeoutEventModule not found")
                return
            }

            Log.d("BaseTimeoutActivity", "Emitting via TimeoutEventModule")
            timeoutModule.emitTimeoutEvent(status, message)
            
        } catch (e: Exception) {
            Log.e("BaseTimeoutActivity", "Error in emit: ${e.message}")
        }
    }

    private fun getReactContext(): ReactContext? {
        return try {
            // First check our cached context
            if (reactContext != null && reactContext?.hasActiveReactInstance() == true) {
                Log.d("BaseTimeoutActivity", "Using cached ReactContext")
                return reactContext
            }

            // If cached context is not available, try to get from current activity's application
            val currentActivity = this
            val reactApp = currentActivity.application as? ReactApplication
            var context = reactApp?.reactNativeHost?.reactInstanceManager?.currentReactContext

            if (context == null) {
                // Try getting from application context
                val appContext = applicationContext.applicationContext
                context = (appContext as? ReactApplication)?.reactNativeHost?.reactInstanceManager?.currentReactContext
            }

            if (context == null) {
                // If still null, try getting from the running react activities
                val reactApp = application as? ReactApplication
                val reactInstanceManager = reactApp?.reactNativeHost?.reactInstanceManager
                val activities = reactInstanceManager?.currentReactContext?.currentActivity
                if (activities != null) {
                    context = reactInstanceManager.currentReactContext
                }
            }

            if (context == null) {
                Log.e("BaseTimeoutActivity", "ReactContext is null from all sources")
            } else {
                Log.d("BaseTimeoutActivity", "Successfully obtained ReactContext")
                // Cache the context for future use
                reactContext = context
            }

            context
        } catch (e: Exception) {
            Log.e("BaseTimeoutActivity", "Error getting ReactContext: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeReactContext()
        startTimeoutTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimeoutTimer()
    }

    private fun startTimeoutTimer() {
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION)
    }

    private fun resetTimeoutTimer() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION)
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }

    protected fun navigateToLaunchScreen() {
        cleanupAndReturnToLaunch(0) // Pass 0 to indicate normal navigation
    }
}


class FrontIdCardActivity : BaseTimeoutActivity() {

    private val PERMISSION_REQUEST_CODE = 10
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private lateinit var cameraExecutor: ExecutorService
    private var previewView: PreviewView? = null
    private lateinit var captureButton: Button
    private var isStarted = false
    private var imageCapture: ImageCapture? = null
    private lateinit var progressBar: FrameLayout
    private var captureInProgress = false
    private var referenceNumber: String? = null
    private lateinit var mediaActionSound: MediaActionSound
    

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            showErrorDialog("Camera permission not granted")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaActionSound = MediaActionSound()
        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK)
        cameraExecutor = Executors.newSingleThreadExecutor()
        referenceNumber = intent.getStringExtra("REFERENCE_NUMBER")
        setupUI()
    }


    override fun cleanupResources() {
        try {
            // Unbind camera use cases
            cameraProvider?.unbindAll()
            camera?.cameraControl?.enableTorch(false)
            camera = null
            preview = null

            // Shutdown executors
            if (!cameraExecutor.isShutdown) {
                cameraExecutor.shutdown()
            }

            // Release media resources
            if (this::mediaActionSound.isInitialized) {
                mediaActionSound.release()
            }

            // Clear UI elements
            previewView = null
            if (this::progressBar.isInitialized) {
                progressBar.visibility = View.GONE
            }
            if (this::captureButton.isInitialized) {
                captureButton.isEnabled = false
            }
            captureInProgress = false
            isStarted = false

            // Cancel any ongoing coroutines
            CoroutineScope(Dispatchers.Main).cancel()

        } catch (e: Exception) {
            Log.e("FrontIdCardActivity", "Error during cleanup: ${e.message}")
        }
    }

    private fun setupUI() {
        // Create a FrameLayout to hold all the UI components
        val frameLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

        }

        // Create the PreviewView for the camera preview
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Instruction TextView
        val instructionTextView = TextView(this).apply {
            text = "Snap the front of your ID"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                900,
                180
            ).apply {
                gravity = Gravity.TOP
                topMargin = 80
                leftMargin = 100
                rightMargin = 50
            }
            setPadding(50, 0, 50, 0)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#80000000"))
                cornerRadius = 30f
            }
        }

        // Capture Button
        captureButton = Button(this).apply {
            text = "Capture Front ID"
            setBackgroundColor(Color.parseColor("#59d5ff"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = FrameLayout.LayoutParams(
                800,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 50
            }
            setPadding(50, 0, 50, 0)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#59d5ff"))
                cornerRadius = 30f
            }
        }

        // ID Card Border Box
        val borderBox = View(this).apply {
            val metrics = windowManager.defaultDisplay.let { display ->
                val displayMetrics = android.util.DisplayMetrics()
                display.getMetrics(displayMetrics)
                displayMetrics
            }

            val width = (metrics.widthPixels * 0.85).toInt()
            val height = (width * 0.63).toInt()

            val params = FrameLayout.LayoutParams(width, height)
            params.gravity = Gravity.CENTER
            layoutParams = params

            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(4, Color.WHITE)
                cornerRadius = 20f
            }
        }

        // Add progress bar (initially invisible)
        progressBar = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#80000000"))
            visibility = View.GONE

            // Add loading indicator
            addView(ProgressBar(this@FrontIdCardActivity).apply {
                indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            })
        }

        // Add all views to the FrameLayout
        frameLayout.addView(previewView)
        frameLayout.addView(borderBox)
        frameLayout.addView(instructionTextView)
        frameLayout.addView(captureButton)
        frameLayout.addView(progressBar)

        // Set the FrameLayout as the content view of the activity
        setContentView(frameLayout)

        captureButton.setOnClickListener {
            takePicture()
        }

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Build Preview use case
                preview = Preview.Builder().build()

                // Add ImageCapture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Create preview view surface provider
                preview?.setSurfaceProvider(previewView?.surfaceProvider)

                // Unbind any bound use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                isStarted = true
            } catch (e: Exception) {
                Log.e("InnoActivity", "Use case binding failed", e)
                showErrorDialog("Failed to start camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        val imageCapture = imageCapture ?: run {
            Log.e("Capture", "Failed to take picture: imageCapture is null")
            return
        }

        // Freeze camera preview by unbinding preview use case
        cameraProvider?.unbind(preview)

        Log.d("Capture", "Starting image capture process")
        captureInProgress = true
        progressBar.visibility = View.VISIBLE
        captureButton.isEnabled = false

        try {
            val outputStream = ByteArrayOutputStream()
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputStream).build()

            // Play shutter sound
            mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)

            imageCapture.takePicture(
                outputFileOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        try {
                            
                           // Convert outputStream to Bitmap
                            val byteArray = outputStream.toByteArray()
                            val originalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                            // Compress the Bitmap
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream) // Compressed to 25%
                            val compressedImage = byteArrayOutputStream.toByteArray()

                            // Pass the compressed image to sendImageToApi
                            sendImageToApi(compressedImage)
                        } catch (e: Exception) {
                            resetCameraPreview()
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                captureButton.isEnabled = true
                                captureInProgress = false
                                showErrorDialog("Error processing image: ${e.message}")
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        resetCameraPreview()
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            captureButton.isEnabled = true
                            captureInProgress = false
                            showErrorDialog("Failed to capture photo: ${exception.message}")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            resetCameraPreview()
            runOnUiThread {
                progressBar.visibility = View.GONE
                captureButton.isEnabled = true
                captureInProgress = false
                showErrorDialog("Error capturing image: ${e.message}")
            }
        }
    }

    private fun resetCameraPreview() {
        try {
            // Rebind preview use case to restart preview
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("InnoActivity", "Error resetting camera preview: ${e.message}")
        }
    }




    private fun sendImageToApi(byteArray: ByteArray) {
        Log.d("sendImageToApi", "Byte array size: ${byteArray.size} bytes")

        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .build()

        val mediaType = "image/jpeg".toMediaType()

        // Show loading dialog
        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rotatedImageData = rotateImage(byteArray)

                val ocrRequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", rotatedImageData.toRequestBody(mediaType))
                    .addFormDataPart("reference_id", referenceNumber!!)
                    .addFormDataPart("side", "front")
                    .build()

                val credentials = Credentials.basic("test", "test")
                val ocrRequest = Request.Builder()
                    .url("https://api.innovitegrasuite.online/process-id")
                    .addHeader("api-key", "testapikey")
                    .header("Authorization", credentials)
                    .post(ocrRequestBody)
                    .build()

                Log.d("sendImageToApi", "Sending OCR request to API")

                val ocrResponse = client.newCall(ocrRequest).execute()

                // Read response body once and store it
                val responseBodyString = ocrResponse.body?.string()

                Log.d("sendImageToApi", "Received OCR response from API")
                Log.d("sendImageToApi", "Response Code: ${ocrResponse.code}")
                Log.d("sendImageToApi", "Response Headers: ${ocrResponse.headers}")
                Log.d("sendImageToApi", "Response Body: $responseBodyString")

                if (ocrResponse.code == 200) {
                    handleSuccessfulOcrResponse(responseBodyString, rotatedImageData)
                } else {
                    throw Exception("Error processing image: ${ocrResponse.message}")
                }

            } catch (e: Exception) {
                Log.e("sendImageToApi", "Error processing image: ${e.message}")
                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    showErrorDialog(e.message ?: "Error1")
                }
            }
        }
    }

    // Updated function to take response as a string instead of Response object
    private suspend fun handleSuccessfulOcrResponse(responseJson: String?, imageData: ByteArray) {
        Log.d("OCRResponse", "handleSuccessfulOcrResponse: $responseJson")
        Log.d("ImageData", "Image Data: $imageData.size")

        try {
            val jsonObject = JSONObject(responseJson ?: "")
            val dataObject = jsonObject.getJSONObject("id_analysis")
            val frontData = dataObject.getJSONObject("front")

            val ocrDataFront = OcrResponseFront(
                fullName = frontData.optString("Full_name", "N/A"),
                dateOfBirth = frontData.optString("Date_of_birth", "N/A"),
                sex = frontData.optString("Sex", "N/A"),
                nationality = frontData.optString("Nationality", "N/A"),
                fcn = frontData.optString("FCN", "N/A"),
                expiryDate = frontData.optString("Expiry_date", "N/A"),
                croppedFace = jsonObject.optString("cropped_face", "N/A"),
                croppedId = jsonObject.optString("cropped_id", "N/A")
            )

            if (ocrDataFront.fullName.isNullOrEmpty() || ocrDataFront.fcn.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    showErrorDialog("Full name or FCN is empty. Please capture the photo again.")
                }
                return
            }



            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            Log.d("bitmapData", "Bitmap: $bitmap")

            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                sharedViewModel.setFrontImage(bitmap)
                sharedViewModel.setOcrData(ocrDataFront)

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Log.d("byteArray", "ByteArray size: ${byteArray.size} bytes")
                navigateToNewActivity(byteArray, ocrDataFront)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                showErrorDialog("Error2: ${e.message}")
            }
        }
    }


    private fun showLoadingDialog() {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingDialog() {
        runOnUiThread {
            progressBar.visibility = View.GONE
            captureButton.isEnabled = true
            captureInProgress = false
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Try Again") { dialog, _ ->
                dialog.dismiss()
                resetCamera()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }


    private fun resetCamera() {
        captureInProgress = false
        progressBar.visibility = View.GONE
        captureButton.isEnabled = true
        isStarted = false
        cameraProvider?.unbindAll()
        previewView = null
        camera = null
        preview = null
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        rootView.removeAllViews()
        setupUI()
        startCamera()
        isStarted = true
    }

    private fun rotateImage(imageData: ByteArray): ByteArray {
        val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        val matrix = Matrix().apply { postRotate(0f) }
        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }

    private fun navigateToNewActivity(byteArray: ByteArray, ocrDataFront: OcrResponseFront) {
        val intent = Intent(this, NewActivity::class.java)
        //intent.putExtra("imageByteArray", byteArray)
        intent.putExtra("ocrProcessingData", ocrDataFront)
        intent.putExtra("referenceNumber", referenceNumber)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaActionSound.release()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}



class NewActivity : BaseTimeoutActivity() {

    private lateinit var sharedViewModel: SharedViewModel

    override fun cleanupResources() {
        try {
            // Clear ViewModel data if needed
            if (this::sharedViewModel.isInitialized) {
                sharedViewModel.clearAllData()
            }
        } catch (e: Exception) {
            Log.e("NewActivity", "Error during cleanup: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        sharedViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SharedViewModel::class.java]


        val referenceNumber = intent.getStringExtra("referenceNumber")

        // Main ScrollView that contains everything
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Main content container
        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // Image view for showing the cropped image
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                980,
                650
            ).apply {
                gravity = Gravity.TOP
                setMargins(0.dpToPx(), 8.dpToPx(), 0.dpToPx(), 8.dpToPx())
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true

             // Apply border using GradientDrawable
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT) // Transparent background
                setStroke(2.dpToPx(), Color.parseColor("#CCCCCC")) // 3dp border with gray color
                cornerRadius = 8.dpToPx().toFloat() // Rounded corners for better look
            }
        }
        contentContainer.addView(imageView)


        // OCR Data processing
         val ocrProcessingData = intent.getSerializableExtra("ocrProcessingData") as? OcrResponseFront
        ocrProcessingData?.croppedId?.let { url ->
            Log.d("FrontImage", "Cropped ID URL: $url")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                    withContext(Dispatchers.Main) {
                        val rotatedBitmap = rotateImage(bitmap, 0f)
                        imageView.setImageBitmap(rotatedBitmap)

                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {

                    }
                }
            }
        }

        ocrProcessingData?.let { ocrData ->
            val ocrTextLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.START
                setPadding(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
                setBackgroundColor(Color.parseColor("#F9F9F9"))

                // Apply a border to the container
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F9F9F9")) // Background color
                    setStroke(2.dpToPx(), Color.parseColor("#CCCCCC")) // Border color and width
                    cornerRadius = 8.dpToPx().toFloat() // Rounded corners
                }
            }

            // Add OCR data TextViews with improved styling
           val textViews = listOf(
            "Full Name: ${ocrData.fullName?.takeIf { it.isNotBlank() } ?: "N/A"}",
            "Date of Birth: ${ocrData.dateOfBirth?.takeIf { it.isNotBlank() } ?: "N/A"}",
            "Sex: ${ocrData.sex?.takeIf { it.isNotBlank() } ?: "N/A"}",
            "Nationality: ${ocrData.nationality?.takeIf { it.isNotBlank() } ?: "N/A"}",
            "FCN: ${ocrData.fcn?.takeIf { it.isNotBlank() } ?: "N/A"}",
            "Date of Expiry: ${ocrData.expiryDate?.takeIf { it.isNotBlank() } ?: "N/A"}"

            ).map { text ->
                TextView(this).apply {
                    setText(text)
                    setTextColor(Color.parseColor("#333333"))
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD) // Bold text for better readability
                    setPadding(0, 12.dpToPx(), 0, 12.dpToPx())


                }
            }

            textViews.forEach { ocrTextLayout.addView(it) }

            contentContainer.addView(ocrTextLayout)


            // Face Image View
            val faceImageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300.dpToPx()
                ).apply {
                    gravity = Gravity.CENTER
                    topMargin = 24.dpToPx()
                    bottomMargin = 24.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F5F5F5"))
                    cornerRadius = 8.dpToPx().toFloat()
                }
                clipToOutline = true
            }
            ocrTextLayout.addView(faceImageView)


            // Loading indicator
            val loadingIndicator = ProgressBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    50.dpToPx(),
                    50.dpToPx()
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            ocrTextLayout.addView(loadingIndicator)

            ocrData.croppedFace?.let { url ->
            Log.d("FrontImage", "Cropped Face URL: $url")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                        withContext(Dispatchers.Main) {
                            faceImageView.setImageBitmap(bitmap)
                            loadingIndicator.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            loadingIndicator.visibility = View.GONE
                        }
                    }

                }
            }


             // Process Back ID button with improved styling
        val processBackIdButton = Button(this).apply {
            text = "Process Back ID Card"
            setTextColor(Color.WHITE)
            textSize = 16f
            background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(Color.parseColor("#59d5ff"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
            }
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            elevation = 4f
            setOnClickListener {
                processBackIdCard( ocrProcessingData,referenceNumber)
            }
        }
        contentContainer.addView(processBackIdButton)
        }


  // Add the content container to ScrollView and set as content
        scrollView.addView(contentContainer)
        setContentView(scrollView)

    }

    private fun rotateImage(source: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }


     private fun processBackIdCard(ocrProcessingData: OcrResponseFront?,referenceNumber: String?) {
        val intent = Intent(this, BackIdCardActivity::class.java).apply {
           // putExtra("imageByteArray", byteArray)
            putExtra("ocrProcessingData", ocrProcessingData)
            putExtra("referenceNumber", referenceNumber)
        }
        startActivity(intent)
        finish()
    }
}




class BackIdCardActivity : BaseTimeoutActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private var reactContext: ReactContext? = null
    private lateinit var progressBar: FrameLayout
    private var promise: Promise? = null
    private lateinit var captureButton: Button
    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String? = null
    private lateinit var mediaActionSound: MediaActionSound
    private lateinit var preview: Preview

    companion object {
        private const val TAG = "BackIdCardActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun isInitialized(): Boolean {
        return reactContext != null && promise != null
    }

    fun initialize(promise: Promise, context: ReactContext) {
        this.promise = promise
        this.reactContext = context
    }

    override fun cleanupResources() {
        try {
            // Release camera resources
            cameraExecutor.shutdown()
            if (this::mediaActionSound.isInitialized) {
                mediaActionSound.release()
            }
            
            // Clear UI elements
            if (this::progressBar.isInitialized) {
                progressBar.visibility = View.GONE
            }
            if (this::captureButton.isInitialized) {
                captureButton.isEnabled = false
            }
            
            // Clear ViewModel data
            if (this::sharedViewModel.isInitialized) {
                sharedViewModel.clearAllData()
            }
        } catch (e: Exception) {
            Log.e("BackIdCardActivity", "Error during cleanup: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize MediaActionSound
        mediaActionSound = MediaActionSound()
        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK)

        referenceNumber = intent.getStringExtra("referenceNumber")


    // Initialize ViewModel
    sharedViewModel = ViewModelProvider(
        this,
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )[SharedViewModel::class.java]



    // Observe StateFlow updates
    lifecycleScope.launch {
        // Observe front image updates
        sharedViewModel.frontImage.collect { bitmap ->
            Log.d("BackIdCardActivityNav", "FrontImage StateFlow updated: $bitmap")
        }

        // Observe OCR data updates
        sharedViewModel.ocrData.collect { data ->
            Log.d("BackIdCardActivityNav", "OCR Data StateFlow updated: $data")
        }
    }

    // Process front image and OCR data from intent
    try {
        // Update front image
        intent.getByteArrayExtra("imageByteArray")?.let { byteArrayFront ->
            val bitmap = BitmapFactory.decodeByteArray(byteArrayFront, 0, byteArrayFront.size)
            if (bitmap != null) {
                sharedViewModel.setFrontImage(bitmap)
                Log.d("BackIdCardActivityNav", "Successfully set front image in ViewModel")
            } else {
                Log.e("BackIdCardActivityNav", "Failed to decode bitmap from byte array")
                sharedViewModel.setError("Failed to decode image")
            }
        }

        // Update OCR data
        val ocrProcessingData = intent.getSerializableExtra("ocrProcessingData") as? OcrResponseFront
        ocrProcessingData?.let {
            sharedViewModel.setOcrData(it)
            Log.d("BackIdCardActivityNav", "Successfully set OCR data in ViewModel: $it")
        } ?: run {
            Log.e("BackIdCardActivityNav", "No OCR data received or invalid data format")
            sharedViewModel.setError("No OCR data received")
        }



    } catch (e: Exception) {
        Log.e("BackIdCardActivityNav", "Error processing data: ${e.message}")
        sharedViewModel.setError("Error processing data: ${e.message}")
    }



        // Create root layout
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }


        // Create PreviewView
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootLayout.addView(previewView)


        // Create instruction text
        val instructionTextView = TextView(this).apply {
            text = "Snap the back of your ID"
            textSize = 22f

            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                900,
                180
            ).apply {
                gravity = Gravity.TOP
                topMargin = 80
                leftMargin = 100
                rightMargin = 50
            }
            setPadding(50, 0, 50, 0)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#80000000"))
                cornerRadius = 30f
            }
        }
        rootLayout.addView(instructionTextView)

            // Create capture button with styling
             captureButton = Button(this).apply {
                text = "Capture Back ID"
                val referenceNumber = intent.getStringExtra("referenceNumber")
                textSize = 18f
                setBackgroundColor(Color.parseColor("#59d5ff"))
                setTextColor(android.graphics.Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(
                    800,  // Match the width from your NewActivity
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    bottomMargin = 50
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 16f
                    setColor(Color.parseColor("#59d5ff"))
                }
               setOnClickListener {
                referenceNumber?.let { ref ->
                    takePhoto(sharedViewModel, ref)
                } ?: run {
                    Log.e(TAG, "Reference number is null")
                    showErrorDialog(Exception("Reference number not found"))
                }
            }
            }
            rootLayout.addView(captureButton)


           // ID Card Border Box
            val borderBox = View(this).apply {
                val displayMetrics = resources.displayMetrics
                val width = (displayMetrics.widthPixels * 0.85).toInt()
                val height = (width * 0.63).toInt()

                layoutParams = FrameLayout.LayoutParams(width, height).apply {
                    gravity = Gravity.CENTER
                }

                background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    setStroke(4, Color.WHITE)
                    cornerRadius = 20f
                }
            }
            rootLayout.addView(borderBox)


            // Progress bar for loading


            progressBar = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#80000000"))
                visibility = View.GONE

                // Add loading indicator
                addView(ProgressBar(context).apply {
                    indeterminateTintList = ColorStateList.valueOf(Color.WHITE)    //progress bar clr. white
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                })
            }
            rootLayout.addView(progressBar)



        setContentView(rootLayout)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    // private fun startCamera() {
    //     val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    //     cameraProviderFuture.addListener({
    //         val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

    //         val preview = Preview.Builder()
    //             .build()
    //             .also {
    //                 it.setSurfaceProvider(previewView.surfaceProvider)
    //             }

    //         imageCapture = ImageCapture.Builder()
    //             .setTargetRotation(previewView.display.rotation)
    //             .setTargetAspectRatio(AspectRatio.RATIO_16_9)
    //             .build()

    //         val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    //         try {
    //             cameraProvider.unbindAll()
    //             cameraProvider.bindToLifecycle(
    //                 this, cameraSelector, preview, imageCapture
    //             )
    //         } catch (exc: Exception) {
    //             Log.e(TAG, "Use case binding failed", exc)
    //             handleError("Camera setup failed", exc)
    //         }
    //     }, ContextCompat.getMainExecutor(this))
    // }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Initialize the class-level preview variable
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                handleError("Camera setup failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                val message = "Permissions not granted by the user."
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                handleError("Camera permissions not granted")
                finish()
            }
        }
    }

    // private fun takePhoto(viewModel: SharedViewModel, referenceNumber: String) {
    //     val imageCapture = imageCapture ?: run {
    //         Log.e("CaptureBack", "ImageCapture is null. Cannot proceed with photo capture.")
    //         return
    //     }

    //     Log.d("CaptureBack", "Starting photo capture process...")
    //     // Disable button immediately
    //     captureButton.isEnabled = false
    //     progressBar.visibility = View.VISIBLE

    //     // Play shutter sound before taking picture
    //     mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)

    //     Log.d("CaptureBack", "Initiating photo capture...")
    //     imageCapture.takePicture(
    //         ContextCompat.getMainExecutor(this),
    //         object : ImageCapture.OnImageCapturedCallback() {
    //             override fun onCaptureSuccess(imageProxy: ImageProxy) {
    //                 Log.d("CaptureBack", "Photo captured successfully. Stopping camera preview...")

    //                 // Convert ImageProxy to Bitmap
    //                 val bitmap = imageProxy.toBitmap()

    //                 // Compress the bitmap (JPEG format, 25% quality)
    //                 val outputStream = ByteArrayOutputStream()
    //                 bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)

    //                 val compressedByteArray = outputStream.toByteArray().also {
    //                     Log.d("CaptureBack", "Compressed image size: ${it.size} bytes")
    //                 }

    //                 // Send compressed image to API
    //                 sendImageToApi(compressedByteArray, viewModel, referenceNumber)

    //                 // Close the ImageProxy
    //                 imageProxy.close()

    //                 // Stop the camera preview
    //                 val cameraProviderFuture = ProcessCameraProvider.getInstance(this@BackIdCardActivity)
    //                 cameraProviderFuture.addListener({
    //                     val cameraProvider = cameraProviderFuture.get()
    //                     cameraProvider.unbindAll() // Unbind the camera preview
    //                     Log.d("CaptureBack", "Camera preview stopped.")
    //                 }, ContextCompat.getMainExecutor(this@BackIdCardActivity))
    //             }

    //             override fun onError(exception: ImageCaptureException) {
    //                 Log.e("CaptureBack", "Photo capture failed: ${exception.message}", exception)
    //                 // Re-enable button and hide progress on error
    //                 runOnUiThread {
    //                     captureButton.isEnabled = true
    //                     progressBar.visibility = View.GONE
    //                 }
    //                 handleError("Photo capture failed", exception)
    //             }
    //         }
    //     )
    // }

    private fun takePhoto(viewModel: SharedViewModel, referenceNumber: String) {
        val imageCapture = imageCapture ?: run {
            Log.e("CaptureBack", "ImageCapture is null. Cannot proceed with photo capture.")
            return
        }

        Log.d("CaptureBack", "Starting photo capture process...")
        // Disable button immediately
        captureButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Freeze the camera preview by unbinding it
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@BackIdCardActivity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbind(preview) // Use the class-level preview variable
            Log.d("CaptureBack", "Camera preview frozen.")
        }, ContextCompat.getMainExecutor(this@BackIdCardActivity))

        // Play shutter sound before taking picture
        mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)

        Log.d("CaptureBack", "Initiating photo capture...")
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    Log.d("CaptureBack", "Photo captured successfully.")

                    // Convert ImageProxy to Bitmap
                    val bitmap = imageProxy.toBitmap()

                    // Compress the bitmap (JPEG format, 25% quality)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)

                    val compressedByteArray = outputStream.toByteArray().also {
                        Log.d("CaptureBack", "Compressed image size: ${it.size} bytes")
                    }

                    // Send compressed image to API
                    sendImageToApi(compressedByteArray, viewModel, referenceNumber)

                    // Close the ImageProxy
                    imageProxy.close()

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CaptureBack", "Photo capture failed: ${exception.message}", exception)
                    // Re-enable button and hide progress on error
                    runOnUiThread {
                        captureButton.isEnabled = true
                        progressBar.visibility = View.GONE
                    }
                    handleError("Photo capture failed", exception)
                }
            }
        )
    }

    // Extension function to convert ImageProxy to Bitmap
    fun ImageProxy.toBitmap(): Bitmap {
        val planeProxy = planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun sendImageToApi(byteArray: ByteArray, viewModel: SharedViewModel, referenceNumber: String) {
    Log.d("sendImageToApi", "Received byte array of size: ${byteArray.size} bytes")

    // Show loading dialog
    showLoadingDialog()

    val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .build()

    val mediaType = "image/jpeg".toMediaType()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Directly process the OCR request without cropping
            processOcrRequest(byteArray, client, mediaType, viewModel, referenceNumber)
        } catch (e: Exception) {
            handleApiError(e)
        }
    }
}

private suspend fun processOcrRequest(
    imageData: ByteArray,
    client: OkHttpClient,
    mediaType: MediaType,
    viewModel: SharedViewModel,
    referenceNumber: String
) {
    Log.d("referenceNumber", "$referenceNumber")

    val ocrRequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "image.jpg", imageData.toRequestBody(mediaType))
        .addFormDataPart("reference_id", referenceNumber)
        .addFormDataPart("side", "back")
        .build()

    val ocrRequest = Request.Builder()
        .url("https://api.innovitegrasuite.online/process-id")
        .addHeader("api-key", "testapikey")
        .header("Authorization", Credentials.basic("test", "test"))
        .post(ocrRequestBody)
        .build()

    try {
        val ocrResponse = client.newCall(ocrRequest).execute()
        if (ocrResponse.isSuccessful) {
            handleSuccessfulOcrResponse(ocrResponse, imageData, viewModel, referenceNumber)
        } else {
            throw Exception("OCR processing failed (Error ${ocrResponse.code})")
        }
    } catch (e: Exception) {
        handleApiError(e)
    }
}

private fun decodeSampledBitmapFromByteArray(imageData: ByteArray, scaleFactor: Float = 0.5f): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

    // Calculate target dimensions (50% of original)
    val targetWidth = (options.outWidth * scaleFactor).toInt()
    val targetHeight = (options.outHeight * scaleFactor).toInt()

    // Decode the actual bitmap with new dimensions
    return BitmapFactory.decodeByteArray(imageData, 0, imageData.size).let { originalBitmap ->
        Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true).also {
            if (it != originalBitmap) {
                originalBitmap.recycle()
            }
        }
    }
}

private suspend fun handleSuccessfulOcrResponse(
    ocrResponse: Response,
    imageData: ByteArray,
    viewModel: SharedViewModel,
    referenceNumber: String
) {
    Log.d("OCRResponse", "handleSuccessfulOcrResponse $ocrResponse")
    var bitmap: Bitmap? = null
    try {
        // Get front data from ViewModel
        val frontImageBitmap = viewModel.frontImage.value
        val frontOcrData = viewModel.ocrData.value

        Log.d("ViewModelFrontId", "Front Image Data: $frontImageBitmap")

        Log.d("ViewModelBackId", "Front Image Data: $frontImageBitmap")
        Log.d("ViewModelBackId", "Front Ocr Data: $frontOcrData")

        val responseJson = ocrResponse.body?.string()
        Log.d("OCRResponse", "OCR Response: $responseJson")

        val jsonObject = JSONObject(responseJson ?: "")
        val dataObject = jsonObject.getJSONObject("id_analysis")
        val backData = dataObject.getJSONObject("back")

        val ocrDataBack = OcrResponseBack(
            Date_of_Expiry = backData.optString("Date_of_Expiry", "N/A"),
            Date_of_Issue = backData.optString("Date_of_Issue", "N/A"),
            Phone_Number = backData.optString("Phone_Number", "N/A"),
            Region = backData.optString("Region", "N/A"),
            Zone = backData.optString("Zone", "N/A"),
            Woreda = backData.optString("Woreda", "N/A"),
            FIN = backData.optString("FIN", "N/A"),
            Nationality = backData.optString("Nationality", "N/A"),
            CroppedId= jsonObject.optString("cropped_id", "N/A")
        )

        // Decode and resize back image
        bitmap = decodeSampledBitmapFromByteArray(imageData)

        // Convert resized bitmap back to byte array with compression
        val compressedBackImageData = ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            stream.toByteArray()
        }

        withContext(Dispatchers.Main) {
            hideLoadingDialog()
            viewModel.setBackImage(bitmap)
            viewModel.setOcrData2(ocrDataBack)

            // Convert and compress front bitmap
            val frontByteArray = frontImageBitmap?.let { frontBitmap ->
                // Calculate dimensions for front image
                val width = (frontBitmap.width * 0.5f).toInt()
                val height = (frontBitmap.height * 0.5f).toInt()

                // Scale front bitmap
                val scaledFrontBitmap = Bitmap.createScaledBitmap(frontBitmap, width, height, true)

                ByteArrayOutputStream().use { stream ->
                    scaledFrontBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    if (scaledFrontBitmap != frontBitmap) {
                        scaledFrontBitmap.recycle()
                    }
                    stream.toByteArray()
                }
            }

            // Navigate with compressed images
            navigateToBackActivity(
                byteArrayBack = compressedBackImageData,
                ocrDataBack = ocrDataBack,
                byteArrayFront = frontByteArray,
                ocrDataFront = frontOcrData,
                referenceNumber
            )
        }
    } catch (e: Exception) {
        handleApiError(e)
    } finally {
        // Clean up any remaining bitmaps
        bitmap?.recycle()
    }
}

// Method to navigate to a new Android Activity
private fun navigateToBackActivity(
    byteArrayBack: ByteArray,
    ocrDataBack: OcrResponseBack,
    byteArrayFront: ByteArray?,
    ocrDataFront: OcrResponseFront?,
    referenceNumber: String
) {
    Log.d("navigateToBackActivity", "ByteArray size: ${byteArrayBack.size}")
    val intent = Intent(this, BackActivity::class.java)
    intent.putExtra("imageByteArray", byteArrayBack) // Pass ByteArray instead of Bitmap
    intent.putExtra("ocrProcessingData", ocrDataBack) // Pass the ocrProcessingData
    intent.putExtra("frontByteArray", byteArrayFront) // Pass ByteArray instead of Bitmap
    intent.putExtra("frontOcrData", ocrDataFront) // Pass the frontOcrData
    intent.putExtra("referenceNumber", referenceNumber) // Pass the referenceNumber
    startActivity(intent)
    finish()
}

    private suspend fun handleApiError(error: Exception) {
        withContext(Dispatchers.Main) {
            hideLoadingDialog()
            showErrorDialog(error)
        }
    }

    private fun showLoadingDialog() {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            captureButton.isEnabled = false
        }
    }

    private fun hideLoadingDialog() {
        runOnUiThread {
            progressBar.visibility = View.GONE
            captureButton.isEnabled = true
        }
    }

    private fun showErrorDialog(error: Exception) {
      Log.e("errorMessage", "$error.message")
        val errorMessage = when {
            error.message?.contains("crop", ignoreCase = true) == true ->
                "OCR Processing Error: No text detected. Ensure ID is clear and well-lit"
            error.message?.contains("OCR", ignoreCase = true) == true ->
                "OCR Processing Error: No text detected. Ensure ID is clear and well-lit"
            else -> "OCR Processing Error: No text detected. Ensure ID is clear and well-lit"
        }

        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(errorMessage)
            .setPositiveButton("Try Again") { dialog, _ ->
                dialog.dismiss()
                startCamera()   // Call the startCamera function here
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()
            .show()
    }


    private fun handleError(message: String, error: Exception? = null) {
        if (isInitialized()) {
            promise?.reject("ERROR", message, error)
        } else {
            Log.e(TAG, "Error: $message", error)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaActionSound.release()
        cameraExecutor.shutdown()
    }
}



class BackActivity : BaseTimeoutActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String? = null


    override fun cleanupResources() {
        try {
            // Clear ViewModel data
            if (this::sharedViewModel.isInitialized) {
                sharedViewModel.clearAllData()
            }
        } catch (e: Exception) {
            Log.e("BackActivity", "Error during cleanup: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


         referenceNumber = intent.getStringExtra("referenceNumber")

        // Initialize ViewModel
        sharedViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SharedViewModel::class.java]

        // Create main ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }




        // Main container inside ScrollView
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // Front ID Section
        addSectionTitle(mainContainer, "Front ID Card")
        val frontImageView = createImageView()
        mainContainer.addView(frontImageView)

        // Back ID Section
        addSectionTitle(mainContainer, "Back ID Card")
        val backImageView = createImageView()
        mainContainer.addView(backImageView)

        // Cropped Face Section
        addSectionTitle(mainContainer, "Cropped Face")
        val croppedFaceView = createImageView()
        mainContainer.addView(croppedFaceView)

        // Front OCR Data Section
        addSectionTitle(mainContainer, "Front ID Data")
        val frontOcrLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createCardBackground()
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }
        mainContainer.addView(frontOcrLayout)

        // Back OCR Data Section
        addSectionTitle(mainContainer, "Back ID Data")
        val backOcrLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createCardBackground()
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }
        mainContainer.addView(backOcrLayout)

        // Process data from intent
        try {
            // Front Image
          (intent.getSerializableExtra("frontOcrData") as? OcrResponseFront)?.let { ocrData ->
              val imageUrl = ocrData.croppedId

              if (imageUrl != null && imageUrl.isNotEmpty()) {
                  CoroutineScope(Dispatchers.IO).launch {
                      try {
                          val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                          withContext(Dispatchers.Main) {
                              frontImageView.setImageBitmap(bitmap)
                              sharedViewModel.setFrontImage(bitmap)
                          }
                      } catch (e: Exception) {
                          Log.e("ImageLoading", "Error loading image: ${e.message}")
                      }
                  }
              }
          }

            // Back Image

            (intent.getSerializableExtra("ocrProcessingData") as? OcrResponseBack)?.let { ocrData ->
              val imageUrl = ocrData.CroppedId

              if (imageUrl != null && imageUrl.isNotEmpty()) {
                  CoroutineScope(Dispatchers.IO).launch {
                      try {
                          val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                          withContext(Dispatchers.Main) {
                              backImageView.setImageBitmap(bitmap)
                              sharedViewModel.setBackImage(bitmap)
                          }
                      } catch (e: Exception) {
                          Log.e("ImageLoading", "Error loading image: ${e.message}")
                      }
                  }
              }
          }

            // Front OCR Data
            val frontOcrData = intent.getSerializableExtra("frontOcrData") as? OcrResponseFront
            frontOcrData?.let { data ->
                sharedViewModel.setOcrData(data)

                // Display Front OCR Data
                addDataRow(frontOcrLayout, "Full Name", data.fullName)
                addDataRow(frontOcrLayout, "Date of Birth", data.dateOfBirth)
                addDataRow(frontOcrLayout, "Sex", data.sex)
                addDataRow(frontOcrLayout, "Nationality", data.nationality)
                addDataRow(frontOcrLayout, "FCN", data.fcn)


                // Loading indicator
            val loadingIndicator = ProgressBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    50.dpToPx(),
                    50.dpToPx()
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            mainContainer.addView(loadingIndicator)

                data.croppedFace?.let { url ->
                  CoroutineScope(Dispatchers.IO).launch {
                      try {
                          val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                          withContext(Dispatchers.Main) {
                              croppedFaceView.setImageBitmap(bitmap)
                              loadingIndicator.visibility = View.GONE
                          }
                      } catch (e: Exception) {
                          withContext(Dispatchers.Main) {
                              loadingIndicator.visibility = View.GONE
                          }
                      }
                  }
              }
            }

            // Back OCR Data
            val backOcrData = intent.getSerializableExtra("ocrProcessingData") as? OcrResponseBack
              backOcrData?.let { data ->
                  sharedViewModel.setOcrData2(data)

                  // Display Back OCR Data with null or empty check
                  addDataRow(backOcrLayout, "Date of Expiry", data.Date_of_Expiry?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Date of Issue", data.Date_of_Issue?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Phone Number", data.Phone_Number?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Region", data.Region?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Zone", data.Zone?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Woreda", data.Woreda?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "FIN", data.FIN?.takeIf { it.isNotBlank() } ?: "N/A")
                  addDataRow(backOcrLayout, "Nationality", data.Nationality?.takeIf { it.isNotBlank() } ?: "N/A")

              }


        } catch (e: Exception) {
            Log.e("BackActivity", "Error processing data: ${e.message}")
            showErrorDialog("Error processing data: ${e.message}")
        }

        // Add Liveliness Detection Button
        val processButton = Button(this).apply {
            text = "Process to Liveliness Detection"
            setBackgroundColor(Color.parseColor("#59d5ff"))
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(Color.parseColor("#59d5ff"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 16.dpToPx())
            }
            setOnClickListener { processLiveliness(referenceNumber) }
        }
        mainContainer.addView(processButton)

        // Set up the view hierarchy
        scrollView.addView(mainContainer)
        setContentView(scrollView)
    }

    // Helper functions
    private fun createImageView(): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300.dpToPx()
            ).apply {
                setMargins(0, 4.dpToPx(), 0, 4.dpToPx())
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            background = createCardBackground()
        }
    }

    private fun addSectionTitle(parent: LinearLayout, title: String) {
        TextView(this).apply {
            text = title
            setTextColor(Color.BLACK)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16.dpToPx(), 0, 8.dpToPx())
            }
            parent.addView(this)
        }
    }

    private fun addDataRow(parent: LinearLayout, label: String, value: String?) {
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4.dpToPx(), 0, 4.dpToPx())
            }

            // Label
            addView(TextView(context).apply {
                text = "$label:"
                setTextColor(Color.GRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8.dpToPx()
                }
            })

            // Value
            addView(TextView(context).apply {
                text = value ?: "N/A"
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            parent.addView(this)
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 8f.dpToPx().toFloat()
            setStroke(2.dpToPx(), Color.parseColor("#CCCCCC"))
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun Float.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun processLiveliness(referenceNumber: String?) {
        Log.d("BackActivity", "Processing to Liveliness...")

        try {
            // Get data from ViewModel
            val frontImage = sharedViewModel.frontImage.value
            val backImage = sharedViewModel.backImage.value
            val frontOcrData = sharedViewModel.ocrData.value
            val backOcrData = sharedViewModel.ocrData2.value

            // Log the data being passed
            Log.d("DataFlowBackActivity", """
                Passing to Liveliness:
                - Front Image Present: ${frontImage != null}
                - Back Image Present: ${backImage != null}
                - Front OCR Data Present: ${frontOcrData != null}
                - Back OCR Data Present: ${backOcrData != null}
            """.trimIndent())

            // Convert bitmaps to byte arrays
            val frontByteArray = frontImage?.let { bitmap ->
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
            }

            val backByteArray = backImage?.let { bitmap ->
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
            }

            // Create intent and pass data
            val intent = Intent(this, Liveliness::class.java)
                // intent.putExtra("frontByteArray", frontByteArray)
                // intent.putExtra("imageByteArray", backByteArray) // back image
                intent.putExtra("frontOcrData", frontOcrData)
                intent.putExtra("ocrProcessingData", backOcrData)
                intent.putExtra("referenceNumber", referenceNumber)


            // Start Liveliness activity
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e("BackActivity", "Error processing data for Liveliness: ${e.message}")
            sharedViewModel.setError("Error preparing data for Liveliness: ${e.message}")
        }
    }

}


class Liveliness : BaseTimeoutActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var overlayImageView: ImageView
    private lateinit var frameLayout: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String? = null
    private val client = OkHttpClient()
    private var lastDetectionTime = 0L
    private val detectionInterval = 500L
    private var isDetectingFaces = false
    private var isPictureTaken = false
    private var frameCounter = 0
    private val frameUpdateFrequency = 10
    private var imageCapture: ImageCapture? = null
    private lateinit var orientationEventListener: OrientationEventListener ;
    private var isPortraitUp = true
    private var orientationDialog: AlertDialog? = null
    private val BLINK_THRESHOLD = 0.6f  // Higher threshold for partial eye closure
    private var isEyeOpen = true // Tracks if eyes are currently open

    private var headMovementTasks = mutableMapOf(
        "Blink detected" to false,
        "Head moved right" to false,
        "Head moved left" to false
    )

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }


    override fun cleanupResources() {
        try {
            // Release camera resources
            cameraExecutor.shutdown()
            
            // Clear UI elements
            if (this::progressBar.isInitialized) {
                progressBar.visibility = View.GONE
            }
            
            // Clear orientation listener
            if (this::orientationEventListener.isInitialized) {
                orientationEventListener.disable()
            }
            
            // Dismiss any dialogs
            orientationDialog?.dismiss()
            
            // Clear ViewModel data
            if (this::sharedViewModel.isInitialized) {
                sharedViewModel.clearAllData()
            }
        } catch (e: Exception) {
            Log.e("Liveliness", "Error during cleanup: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        referenceNumber = intent.getStringExtra("referenceNumber")

        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setupUI()
        initializeViewModel()
        cameraExecutor = Executors.newSingleThreadExecutor()



        // Initialize orientation listener
        orientationEventListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                when {
                    // Device is roughly vertical and upright (portrait up)
                    (orientation in 315..360 || orientation in 0..45) -> {
                        if (!isPortraitUp) {
                            isPortraitUp = true
                            hideOrientationDialog()
                            // Restart the face-matching process when returning to portrait mode
                            restartFaceMatchingProcess()
                        }
                    }
                    // Any other orientation
                    else -> {
                        if (isPortraitUp) {
                            isPortraitUp = false
                            showOrientationDialog()

                            // Restart the face-matching process when orientation changes
                            restartFaceMatchingProcess()
                        }
                    }
                }
            }
        }

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }


        // Enable the orientation listener
        orientationEventListener?.enable()
        }


    private fun restartFaceMatchingProcess() {
        // Reset the face detection tasks
        resetTasks()

        // Restart the camera
        startCamera()

        isPictureTaken = false

        // Clear the overlay image
        runOnUiThread {
            overlayImageView.setImageBitmap(null)
        }

        // Optionally, you can also reset the instruction text
        showInstructionText("Please blink your eyes")
    }

    private fun initializeViewModel() {
        sharedViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SharedViewModel::class.java]

        try {

            // Process OCR data
            val frontOcrData = intent.getSerializableExtra("frontOcrData") as? OcrResponseFront
            frontOcrData?.let {
                sharedViewModel.setOcrData(it)
                Log.d("LivelinessData", "Front OCR data set: ${it.fullName}")
            }

            val backOcrData = intent.getSerializableExtra("ocrProcessingData") as? OcrResponseBack
            backOcrData?.let {
                sharedViewModel.setOcrData2(it)
                Log.d("LivelinessData", "Back OCR data set: ${it.FIN}")
            }


        } catch (e: Exception) {
            Log.e("Liveliness", "Error initializing ViewModel: ${e.message}")
            showErrorDialog("Failed to initialize: ${e.message}")
        }
    }

    private fun setupUI() {
        frameLayout = FrameLayout(this)

        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }

        overlayImageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }


        frameLayout.addView(previewView)
        frameLayout.addView(overlayImageView)
        frameLayout.addView(progressBar)
        setContentView(frameLayout)
    }


    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Unbind all previous use cases
            cameraProvider.unbindAll()


            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Image analysis use case
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
                }

            // Select front camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
                showErrorDialog("Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        private val faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()

            // Process frames only every 100ms
            if (currentTime - lastDetectionTime >= 100) {
                val mediaImage = imageProxy.image
                if (mediaImage != null && !isDetectingFaces) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    isDetectingFaces = true
                    lastDetectionTime = currentTime

                    faceDetector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isEmpty()) {
                                showInstructionText("No face detected. Please position your face in the frame.")
                                if (headMovementTasks.any { it.value }) {
                                    resetTasks()
                                    Log.d("FaceDetection", "Face lost - progress reset")
                                }
                                drawFacesOnOverlay(emptyList())
                            } else {
                                val primaryFace = faces.maxByOrNull { face ->
                                    val size = face.boundingBox.width() * face.boundingBox.height()
                                    val centerDistance = calculateCenterProximity(face.boundingBox)
                                    size - centerDistance
                                }

                                if (primaryFace != null) {
                                    processDetectedFace(primaryFace)
                                    drawFacesOnOverlay(listOf(primaryFace))
                                } else {
                                    drawFacesOnOverlay(emptyList())
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FaceDetection", "Face detection failed", e)
                        }
                        .addOnCompleteListener {
                            isDetectingFaces = false
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            } else {
                // Skip this frame if 100ms have not passed
                imageProxy.close()
            }
        }
    }

    private fun calculateCenterProximity(bounds: Rect): Int {
        val screenWidth = overlayImageView.width
        val screenHeight = overlayImageView.height
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2

        val faceCenterX = bounds.centerX()
        val faceCenterY = bounds.centerY()

        return (faceCenterX - centerX) * (faceCenterX - centerX) +
               (faceCenterY - centerY) * (faceCenterY - centerY)
    }


    private fun drawFacesOnOverlay(faces: List<Face>) {
        try {
            // Check if all tasks are completed
            if (headMovementTasks.all { it.value }) {
                // Hide the bounding box by clearing the overlay
                runOnUiThread {
                    overlayImageView.setImageBitmap(null)
                }
                return
            }

            // Create a mutable bitmap with the same dimensions as the overlayImageView
            val mutableBitmap = Bitmap.createBitmap(
                overlayImageView.width,
                overlayImageView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(mutableBitmap)
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }

            // If no faces are detected, clear the overlay
            if (faces.isEmpty()) {
                runOnUiThread {
                    overlayImageView.setImageBitmap(null)
                }
                return
            }

            // Draw bounding boxes for each detected face
            for (face in faces) {
                val bounds = face.boundingBox

                // Shift the bounding box slightly to the right
                val adjustedBounds = Rect(
                    bounds.left + 20,  // Move 20 pixels to the right
                    bounds.top + 1500,
                    bounds.right + 600,
                    bounds.bottom + 200
                )

                paint.color = Color.GREEN
                canvas.drawRect(adjustedBounds, paint)
            }

            // Update the overlayImageView with the new bitmap on the UI thread
            runOnUiThread {
                overlayImageView.setImageBitmap(mutableBitmap)
            }

            // Check if all tasks are completed and take a picture if necessary
            if (!isPictureTaken && headMovementTasks.all { it.value }) {
                takePicture()
            }
        } catch (e: Exception) {
            Log.e("FaceOverlay", "Error drawing face overlay: ${e.message}")
        }
    }



    private fun processDetectedFace(face: Face) {
        val headEulerAngleY = face.headEulerAngleY
        val leftEyeOpenProb = face.leftEyeOpenProbability ?: -0.9f
        val rightEyeOpenProb = face.rightEyeOpenProbability ?: -0.9f

        // Calculate average eye openness
        val avgEyeOpenness = (leftEyeOpenProb + rightEyeOpenProb) / 2

        // Detect blink
        if (avgEyeOpenness < BLINK_THRESHOLD && isEyeOpen) {
            // Transition from open to closed (blink detected)
            isEyeOpen = false

            // Update task only if blink is detected for the first time
            if (!headMovementTasks["Blink detected"]!!) {
                updateTask("Blink detected")
                showInstructionText("Please move your head to the Left")
                Log.d("FaceDetection", "Blink detected - Eye openness: $avgEyeOpenness")
            }
        } else if (avgEyeOpenness >= BLINK_THRESHOLD && !isEyeOpen) {
            // Transition from closed to open (eyes are open again)
            isEyeOpen = true
        }

        // Handle head movements (existing logic)
        when {
            headMovementTasks["Blink detected"]!! &&
                    !headMovementTasks["Head moved right"]!! &&
                    headEulerAngleY > 10 -> {
                updateTask("Head moved right")
                showInstructionText("Please move your head to the Right")
                Log.d("FaceDetection", "Head turned right - Angle: $headEulerAngleY")
            }

            headMovementTasks["Head moved right"]!! &&
                    !headMovementTasks["Head moved left"]!! &&
                    headEulerAngleY < -10 -> {
                updateTask("Head moved left")
                showInstructionText("Perfect! Taking your Photo...")
                Log.d("FaceDetection", "Head turned left - Angle: $headEulerAngleY")
                if (!isPictureTaken) {
                    takePicture()
                }
            }
        }

        // Show initial instruction if no blink detected yet
        if (!headMovementTasks["Blink detected"]!!) {
            showInstructionText("Please blink your Eyes")
        }
    }



    private fun takePicture() {

      if (!isPortraitUp) {
        showOrientationDialog()
        return
    }

        isPictureTaken = true
        val imageCapture = imageCapture ?: return

        showCountdownUI {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {

                            if (!isPortraitUp) {
                                image.close()
                                restartFaceMatchingProcess()
                                return
                            }


                            val bitmap = image.toBitmap()
                            val rotationDegrees = image.imageInfo.rotationDegrees
                            val byteArray = bitmap.toByteArray()
                            Log.e("CaptureByte", "Captured image orientation: ${rotationDegrees} Degree")
                            image.close()

                            CoroutineScope(Dispatchers.IO).launch {
                                matchFaces(byteArray , rotationDegrees)
                            }
                        } catch (e: Exception) {
                            Log.e("Capture", "Failed to process captured image", e)
                            showErrorDialog("Failed to process captured image: ${e.message}")
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("Capture", "Image capture failed", exception)
                        //showErrorDialog("Failed to capture image: ${exception.message}")
                        isPictureTaken = false
                    }
                }
            )
        }
    }


   


    private fun showCountdownUI(onCountdownComplete: () -> Unit) {
        runOnUiThread {
            val countdownTextView = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    500, 700
                ).apply {
                    gravity = Gravity.CENTER
                }
                textSize = 95f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#80000000"))
                    cornerRadius = 30f
                }
                setPadding(40, 20, 40, 20)
            }

            frameLayout.addView(countdownTextView)

            var countdown = 3
            val handler = Handler(Looper.getMainLooper())

            val countdownRunnable = object : Runnable {
                override fun run() {
                    if (countdown > 0) {
                        countdownTextView.text = countdown.toString()
                        countdown--
                        handler.postDelayed(this, 1000)
                    } else {
                        frameLayout.removeView(countdownTextView)
                        onCountdownComplete()
                    }
                }
            }
            handler.post(countdownRunnable)
        }
    }

    private suspend fun downloadReferenceImage(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                URL(url).openConnection().let { connection ->
                    (connection as HttpURLConnection).apply {
                        connectTimeout = 60000
                        readTimeout = 60000
                        doInput = true
                        requestMethod = "GET"
                    }.inputStream.use { it.readBytes() }
                }
            } catch (e: Exception) {
                throw Exception("Failed to download reference image: ${e.message}")
            }
        }
    }



    private suspend fun matchFaces(selfieBytes: ByteArray, rotationDegrees: Int) {
        withContext(Dispatchers.Main) {
            showLoadingDialog()
            val frontOcrData = sharedViewModel.ocrData.value

            // Validate reference number
            if (referenceNumber.isNullOrEmpty()) {
                throw Exception("Reference number is missing")
            }

            // Log OCR data
            Log.d("FaceMatching", "OCR Data: $frontOcrData")
            Log.d("FaceMatching", "Reference Number: $referenceNumber")

            if (frontOcrData?.croppedFace.isNullOrEmpty()) {
                throw Exception("Missing reference face image")
            }

            // Download reference image
            Log.d("FaceMatching", "Downloading reference image from: ${frontOcrData!!.croppedFace}")
            val referenceImageBytes = withContext(Dispatchers.IO) {
                downloadReferenceImage(frontOcrData.croppedFace!!)
            }
            Log.d("FaceMatching", "Reference image size: ${referenceImageBytes.size} bytes")
            Log.d("FaceMatching", "Selfie image size: ${selfieBytes.size} bytes")

            // Decode selfieBytes to Bitmap
            val selfieBitmap = BitmapFactory.decodeByteArray(selfieBytes, 0, selfieBytes.size)

            // Correct the orientation of the selfie image
            val correctedSelfieBitmap = correctImageOrientation(selfieBitmap, rotationDegrees)  // Example: 270 degrees rotation

            // Convert corrected Bitmap back to ByteArray
            val byteArrayOutputStream = ByteArrayOutputStream()
            correctedSelfieBitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)  //compressed to 25%
            val rotatedSelfieBytes = byteArrayOutputStream.toByteArray()

            // Create request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "candidate_image",
                    "${referenceNumber}_selfie.jpg",
                    rotatedSelfieBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart(
                    "reference_image",
                    "${referenceNumber}_profile_image.jpg",
                    referenceImageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("image_id", referenceNumber!!)
                .build()


            // Make request
            val request = Request.Builder()
                .url("https://api.innovitegrasuite.online/neuro/verify")
                .post(requestBody)
                .build()

            Log.d("FaceMatching", "Sending request to server...")
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            // Handle response
            handleMatchingResponse(response ,referenceNumber!!)
        }
    }

    private fun correctImageOrientation(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()

        val display =
                (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val rotation = display.rotation

        matrix.postScale(-1f, 1f)

        val deviceRotationAngle =
                when (rotation) {
                    Surface.ROTATION_0 -> 0
                    Surface.ROTATION_90 -> 90
                    Surface.ROTATION_180 -> 180
                    Surface.ROTATION_270 -> 270
                    else -> 0
                }

        val totalRotation =
                when {
                    rotationDegrees == 270 -> (90 + deviceRotationAngle) % 360
                    else -> (deviceRotationAngle + rotationDegrees) % 360
                }

        Log.d(
                "OrientationDebug",
                """
            Device Rotation: $rotation
            Device Angle: $deviceRotationAngle
            Camera Rotation: $rotationDegrees
            Total Rotation: $totalRotation
            Is Front Camera: true
            Special Case (270°): ${rotationDegrees == 270}
        """.trimIndent()
        )
        matrix.postRotate(totalRotation.toFloat())

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e("LivelinessActivity", "Error rotating bitmap: ${e.message}")
            bitmap
        }
    }

    private fun handleMatchingResponse(response: Response ,referenceNumber: String) {
        Log.d("FaceMatching", "handleMatchingResponse: ${referenceNumber}")
        try {
            // Log raw response
            val responseBody = response.body?.string()
            hideLoadingDialog()
            Log.d("FaceMatching", "Response code: ${response.code}")
            Log.d("FaceMatching", "Response body: $responseBody")

            // Extract verification_status from the response body
            val verificationStatus = try {
                val jsonObject = JSONObject(responseBody)
                jsonObject.getString("verification_status")
            } catch (e: Exception) {
                Log.e("FaceMatching", "Error parsing response: ${e.message}", e)
                "Unknown" // Default value if parsing fails
            }

            // Show the verification status in an alert dialog
            //showAlertDialog("Face Matching: $verificationStatus")
            val intent = Intent(this, ReactNativeActivity::class.java)
              // Put the byte arrays
            intent.putExtra("referenceNumber", referenceNumber)
            intent.putExtra("verificationStatus", verificationStatus)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e("FaceMatching", "Error handling response: ${e.message}", e)
           // showAlertDialog("Error: ${e.message}")
        }
    }




    // Utility Functions
    private fun Bitmap.toByteArray(): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.toByteArray()
        }
    }



    // UI Helper Functions
    private fun showLoadingDialog() {
        progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoadingDialog() {
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showInstructionText(instruction: String) {
        runOnUiThread {
            val textView = TextView(this).apply {
                text = instruction
                textSize = 18f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(900, 200).apply {
                    gravity = Gravity.TOP
                    topMargin = 80
                    leftMargin = 100
                    rightMargin = 50
                }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#80000000"))
                    cornerRadius = 30f
                }
                setPadding(50, 0, 50, 0)
            }

            frameLayout.findViewWithTag<TextView>("instruction")?.let {
                frameLayout.removeView(it)
            }
            textView.tag = "instruction"
            frameLayout.addView(textView)
        }
    }


    private fun showErrorDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun resetTasks() {
        headMovementTasks = mutableMapOf(
            "Blink detected" to false,
            "Head moved right" to false,
            "Head moved left" to false
        )
         isPictureTaken = false
    }

    private fun updateTask(taskName: String) {
        headMovementTasks[taskName] = true
        Log.d("FaceDetection", "Task updated: $taskName = true")
    }

    private fun handleAnyError(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Try Again") { dialog, _ ->
                    dialog.dismiss()
                    resetTasks()
                    isPictureTaken = false
                    startCamera()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .show()
        }
    }

    // Permission Handling
    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                showErrorDialog("Camera permission is required")
            }
        }
    }


    private fun showOrientationDialog() {
        if (orientationDialog?.isShowing != true) {
            orientationDialog = AlertDialog.Builder(this)
                .setTitle("Portrait Mode Only")
                .setMessage("Please hold your device upright for a proper selfie.")
                .setCancelable(false)
                .create()
            orientationDialog?.show()
        }
    }

    private fun hideOrientationDialog() {
        orientationDialog?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener.disable()
        hideOrientationDialog()
    }

    // Lifecycle Methods
    override fun onDestroy() {
        super.onDestroy()
        hideOrientationDialog()
        cameraExecutor.shutdown()
    }
}


class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val _timeoutStatus = MutableLiveData<Pair<Int, String?>>()
    val timeoutStatus: LiveData<Pair<Int, String?>> get() = _timeoutStatus
    private val _frontImage = MutableStateFlow<Bitmap?>(null)
    private val _backImage = MutableStateFlow<Bitmap?>(null)
    private val _ocrData = MutableStateFlow<OcrResponseFront?>(null)
    private val _ocrData2 = MutableStateFlow<OcrResponseBack?>(null)
    private val _errorState = MutableStateFlow<String?>(null)
    private val _sessionTimeout = MutableStateFlow<Int?>(null) 
    private val _isSessionTimeout = MutableStateFlow(false)

    val frontImage: StateFlow<Bitmap?> get() = _frontImage
    val backImage: StateFlow<Bitmap?> get() = _backImage
    val ocrData: StateFlow<OcrResponseFront?> get() = _ocrData
    val ocrData2: StateFlow<OcrResponseBack?> get() = _ocrData2
    val errorState: StateFlow<String?> get() = _errorState
    val sessionTimeout: StateFlow<Int?> get() = _sessionTimeout // Expose sessionTimeout
    val isSessionTimeout: StateFlow<Boolean> get() = _isSessionTimeout
    

    fun setFrontImage(bitmap: Bitmap) {
        Log.d("ViewModel", "Updating frontImage with new Bitmap: $bitmap")
        _frontImage.value = bitmap
    }

    fun setBackImage(bitmap: Bitmap) {
        Log.d("ViewModel", "Updating BackImage with new Bitmap: $bitmap")
        _backImage.value = bitmap
    }

    fun setOcrData(datas: OcrResponseFront) {
        Log.d("ViewModel", "Updating OCRFront with new data: $datas")
        _ocrData.value = datas
    }

    fun setOcrData2(datas: OcrResponseBack) {
        Log.d("ViewModel", "Updating OCRBack with new data: $datas")
        _ocrData2.value = datas
    }

    fun setError(message: String) {
        Log.e("ViewModel", "Error occurred: $message")
        _errorState.value = message
    }

    fun clearError() {
        _errorState.value = null
    }

     fun setSessionTimeout(status: Int, message: String?) {
        _timeoutStatus.value = Pair(status, message)
    }

    fun updateSessionTimeout(isTimeout: Boolean) {
        _isSessionTimeout.value = isTimeout
    }

    fun clearAllData() {
        _frontImage.value = null
        _backImage.value = null
        _ocrData.value = null
        _ocrData2.value = null
        _errorState.value = null
        _sessionTimeout.value = null 
    }
}



data class OcrResponseFront(
    val fullName: String,
    val dateOfBirth: String,
    val sex: String,
    val nationality: String,
    val fcn: String,
    val expiryDate: String,
    val croppedFace: String,
    val croppedId: String,
) : Serializable


data class OcrResponseBack(
        val Date_of_Expiry: String,
        val Date_of_Issue: String,
        val Phone_Number: String,
        val Region: String,
        val Zone: String,
        val Woreda: String,
        val FIN: String,
        val Nationality: String,
        val CroppedId: String,

) : Serializable



fun OcrResponseFront.toMap(): Map<String, Any> {
    return mapOf(
        "fullName" to (fullName ?: "N/A"),
        "dateOfBirth" to (dateOfBirth ?: "N/A"),
        "sex" to (sex ?: "N/A"),
        "nationality" to (nationality ?: "N/A"),
        "fcn" to (fcn ?: "N/A"),
        "expiryDate" to (expiryDate ?: "N/A"),
        "croppedFace" to (croppedFace ?: "N/A"),
        "croppedId" to (croppedId ?: "N/A")
    )
}

fun OcrResponseBack.toMap(): Map<String, Any> {
    return mapOf(
        "dateOfExpiry" to (Date_of_Expiry ?: "N/A"),
        "dateOfIssue" to (Date_of_Issue ?: "N/A"),
        "phoneNumber" to (Phone_Number ?: "N/A"),
        "region" to (Region ?: "N/A"),
        "zone" to (Zone ?: "N/A"),
        "woreda" to (Woreda ?: "N/A"),
        "fin" to (FIN ?: "N/A"),
        "nationality" to (Nationality ?: "N/A"),
        "croppedId" to (CroppedId ?: "N/A"),
    )
}

class InnoModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "InnoModule"
    }

    @ReactMethod
    fun showEkycUI(promise: Promise) {
        try {
            // Implementation for showing eKYC UI
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
}
