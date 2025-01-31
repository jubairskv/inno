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
import androidx.lifecycle.LifecycleOwner
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






class InnoModule(reactContext: ReactApplicationContext) :ReactContextBaseJavaModule(reactContext), PermissionListener {

    private val PERMISSION_REQUEST_CODE = 10
    private var permissionPromise: Promise? = null
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
    private var referenceNumber: String = ""


     private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(reactContext.applicationContext as Application)
            .create(SharedViewModel::class.java)
    }


    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun getName(): String {
        return NAME
    }


    companion object {
        const val NAME = "Inno"
    }

   @ReactMethod
    fun getDummyText(dateString: String, promise: Promise) {
        // Store the dateString as reference number
        referenceNumber = dateString
        Log.d("CameraModule", "Received reference number: $referenceNumber")

        val response = "Received date: $dateString. Hello from Native Module!"
        promise.resolve(response)

    }

    @ReactMethod
    fun requestCameraPermission(promise: Promise) {
        val activity = currentActivity as? PermissionAwareActivity
            ?: return promise.reject("NO_ACTIVITY", "No activity found")

        permissionPromise = promise

        activity.requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE,
            this
        )
    }

    @ReactMethod
    fun checkCameraPermission(promise: Promise) {
        val permission = ContextCompat.checkSelfPermission(
            reactApplicationContext,
            Manifest.permission.CAMERA
        )
        promise.resolve(permission == PackageManager.PERMISSION_GRANTED)
    }

    @ReactMethod
    fun startCamera(promise: Promise ) {
        if (isStarted) {
            promise.resolve(true)
            return
        }

        val activity = currentActivity ?: return promise.reject("NO_ACTIVITY", "No activity found")

        // Check and request permission if needed
        if (ContextCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            val permissionAwareActivity = activity as? PermissionAwareActivity
                ?: return promise.reject("NO_ACTIVITY", "No activity found")

            permissionAwareActivity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE,
                object : PermissionListener {
                    override fun onRequestPermissionsResult(
                        requestCode: Int,
                        permissions: Array<String>,
                        grantResults: IntArray
                    ): Boolean {
                        if (requestCode == PERMISSION_REQUEST_CODE) {
                            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                // Permission granted, start camera
                                activity.runOnUiThread {
                                    setupUI(activity, promise)
                                    isStarted = true
                                }
                                return true
                            } else {
                                promise.reject("PERMISSION_DENIED", "Camera permission not granted")
                                return true
                            }
                        }
                        return false
                    }
                }
            )
            return
        }

        // Permission already granted, start camera
        activity.runOnUiThread {
            setupUI(activity, promise )
            isStarted = true
        }
    }

    private fun setupUI(activity: Activity, promise: Promise) {
        // Create a FrameLayout to hold all the UI components
        val frameLayout = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Create the PreviewView for the camera preview
        previewView = PreviewView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Instruction TextView
        val instructionTextView = TextView(activity).apply {
            text = "Take a Picture of Front side of ID Card"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                900,
                250
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
        captureButton = Button(activity).apply {
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
        val borderBox = View(activity).apply {
            val metrics = activity.windowManager.defaultDisplay.let { display ->
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
        progressBar = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#80000000"))
            visibility = View.GONE

            // Add loading indicator
            addView(ProgressBar(activity).apply {
                indeterminateTintList = ColorStateList.valueOf(Color.WHITE)    //progress bar clr. white
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
        frameLayout.addView(progressBar)  // Add progress bar last so it appears on top

        // Set the FrameLayout as the content view of the activity
        activity.setContentView(frameLayout)

         captureButton.setOnClickListener {
            takePicture(promise, sharedViewModel,referenceNumber)
        }

        // Start the camera preview
        startCameraX(promise)
    }

    private fun startCameraX(promise: Promise) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(reactApplicationContext)

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
                    currentActivity as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // // Set up capture button click listener
                // captureButton.setOnClickListener {
                //     if (!captureInProgress) {
                //         takePicture(sharedViewModel)
                //     }
                // }

                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("CAMERA_ERROR", "Failed to start camera: ${e.message}")
                Log.e(NAME, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(reactApplicationContext))
    }

    @ReactMethod
    fun stopCamera(promise: Promise) {
        if (!isStarted) {
            promise.resolve(true)
            return
        }

        try {
            val activity = currentActivity ?: return promise.reject("NO_ACTIVITY", "No activity found")

            activity.runOnUiThread {
                // Remove camera UI
                val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
                rootView.removeAllViews()

                // Unbind use cases
                cameraProvider?.unbindAll()
                previewView = null
                camera = null
                preview = null
                isStarted = false
            }

            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("CAMERA_ERROR", "Failed to stop camera: ${e.message}")
        }
    }

    @ReactMethod
    fun toggleCamera(promise: Promise) {
        try {
            if (isStarted) {
                stopCamera(promise)
            } else {
                startCamera(promise)
            }
        } catch (e: Exception) {
            promise.reject("CAMERA_ERROR", "Failed to toggle camera: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() &&
                         grantResults[0] == PackageManager.PERMISSION_GRANTED
            permissionPromise?.resolve(granted)
            permissionPromise = null
            return true
        }
        return false
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }

    private fun takePicture(promise: Promise, sharedViewModel: SharedViewModel,referenceNumber: String) {
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

            imageCapture.takePicture(
                outputFileOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        try {
                            val byteArray = outputStream.toByteArray()
                            sendImageToApi(byteArray, sharedViewModel, promise ,referenceNumber)
                        } catch (e: Exception) {
                            resetCameraPreview()
                            currentActivity?.runOnUiThread {
                                progressBar.visibility = View.GONE
                                captureButton.isEnabled = true
                                captureInProgress = false
                                showErrorDialog("Error processing image: ${e.message}", promise)
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        resetCameraPreview()
                        currentActivity?.runOnUiThread {
                            progressBar.visibility = View.GONE
                            captureButton.isEnabled = true
                            captureInProgress = false
                            showErrorDialog("Failed to capture photo: ${exception.message}", promise)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            resetCameraPreview()
            currentActivity?.runOnUiThread {
                progressBar.visibility = View.GONE
                captureButton.isEnabled = true
                captureInProgress = false
                showErrorDialog("Error capturing image: ${e.message}", promise)
            }
        }
    }

    // Add this helper function to reset camera preview
    private fun resetCameraPreview() {
        try {
            // Rebind preview use case to restart preview
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider?.bindToLifecycle(
                currentActivity as LifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e(NAME, "Error resetting camera preview: ${e.message}")
        }
    }

private fun sendImageToApi(
    byteArray: ByteArray,
    sharedViewModel: SharedViewModel,
    promise: Promise,
    referenceNumber: String
) {
    Log.d("sendImageToApi", "Byte array size: ${byteArray.size} bytes")

    val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .build()

    val mediaType = "image/jpeg".toMediaType()
    val croppingRequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "image.jpg", byteArray.toRequestBody(mediaType))
        .build()

    val croppingRequest = Request.Builder()
        .url("https://api.innovitegrasuite.online/crop-aadhar-card/")
        .post(croppingRequestBody)
        .build()

    // Show loading dialog
    showLoadingDialog()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // First API call: Cropping
            val croppingResponse = client.newCall(croppingRequest).execute()
            if (croppingResponse.isSuccessful) {
                Log.d("sendImageToApi", "Cropping successful, proceeding to OCR")

                val croppedImageData = croppingResponse.body?.bytes()
                if (croppedImageData != null) {
                     val rotatedImageData = rotateImage(croppedImageData)
                    // Second API call: OCR Processing
                    val ocrRequestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg", rotatedImageData.toRequestBody(mediaType))
                        .addFormDataPart("reference_id", referenceNumber)
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
                    Log.d("sendImageToApi", """
                        OCR Request Details:
                        - URL: ${ocrRequest.url}
                        - Headers: ${ocrRequest.headers}
                        - Body size: ${ocrRequestBody.contentLength()} bytes
                    """.trimIndent())


                    val ocrResponse = client.newCall(ocrRequest).execute()

                     // Detailed OCR Response Logging
                    Log.d("sendImageToApi", """
                        OCR Response Details:
                        Status Code: ${ocrResponse.code}
                        Headers: ${ocrResponse.headers}
                        Message: ${ocrResponse.message}
                    """.trimIndent())

                    if (ocrResponse.isSuccessful) {
                        handleSuccessfulOcrResponse(ocrResponse, croppedImageData, sharedViewModel,promise,referenceNumber)
                    } else {
                        throw Exception("OCR API error: ${ocrResponse.code}")
                    }
                } else {
                    throw Exception("Cropping response body is null.")
                }
            } else {
                throw Exception("Cropping API error: ${croppingResponse.code}")
            }
        } catch (e: Exception) {
            Log.e("sendImageToApi", "Error processing image: ${e.message}")
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                showErrorDialog(e.message ?: "Unknown error",promise)
                //promise.reject("API_ERROR", e.message ?: "Unknown error")
            }
        }
    }
}
    private suspend fun handleSuccessfulOcrResponse(
        ocrResponse: Response,
        croppedImageData: ByteArray,
        sharedViewModel: SharedViewModel,
        promise: Promise,
        referenceNumber: String
      ) {
        Log.d("OCRResponse", "handleSuccessfulOcrResponse${ocrResponse}")
        try {
            val responseJson = ocrResponse.body?.string()
            Log.d("OCRResponse", "OCR Response: $responseJson")

            val jsonObject = JSONObject(responseJson ?: "")
            val dataObject = jsonObject.getJSONObject("id_analysis")
            val frontData = dataObject.getJSONObject("front")

            val ocrDataFront = OcrResponseFront(
                fullName = frontData.getString("Full_name"),
                dateOfBirth = frontData.getString("Date_of_birth"),
                sex = frontData.getString("Sex"),
                nationality = frontData.getString("Nationality"),
                fcn = frontData.getString("FCN"),
                croppedFace = jsonObject.optString("cropped_face", null)
            )

            val ocrDataBack = OcrResponseFront(
                fullName = frontData.getString("Full_name"),
                dateOfBirth = frontData.getString("Date_of_birth"),
                sex = frontData.getString("Sex"),
                nationality = frontData.getString("Nationality"),
                fcn = frontData.getString("FCN"),
                croppedFace = jsonObject.optString("cropped_face", null)
            )

            val bitmap = BitmapFactory.decodeByteArray(croppedImageData, 0, croppedImageData.size)

            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                sharedViewModel.setFrontImage(bitmap)
                sharedViewModel.setOcrData(ocrDataFront)

                // Pass the cropped image to the next activity
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                navigateToNewActivity(byteArray, ocrDataFront,sharedViewModel,referenceNumber)

                // promise.resolve("OCR processing completed successfully.")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                showErrorDialog("Error processing OCR response: ${e.message}",promise)
                // promise.reject("OCR_ERROR", e.message ?: "Unknown error")
            }
        }
      }

        // Add these helper functions
        private fun showLoadingDialog() {
            currentActivity?.runOnUiThread {
                progressBar.visibility = View.VISIBLE
            }
        }

        private fun hideLoadingDialog() {
            currentActivity?.runOnUiThread {
                progressBar.visibility = View.GONE
                captureButton.isEnabled = true
                captureInProgress = false
            }
        }

        private fun showErrorDialog(message: String, promise: Promise) {
            val activity = currentActivity ?: return

            AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Try Again") { dialog, _ ->
                    dialog.dismiss()

                    // Reset all states
                    captureInProgress = false
                    progressBar.visibility = View.GONE
                    captureButton.isEnabled = true
                    isStarted = false

                    // Clean up camera resources
                    cameraProvider?.unbindAll()
                    previewView = null
                    camera = null
                    preview = null

                    // Clear the existing view
                    val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    rootView.removeAllViews()

                    // Start camera again
                    activity.runOnUiThread {
                        setupUI(activity, promise )
                        startCameraX(promise)
                        isStarted = true
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    activity.finish()
                }
                .setCancelable(false)
                .create()
                .show()
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
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return outputStream.toByteArray()
        }

        // Method to navigate to a new Android Activity
        private fun navigateToNewActivity(byteArray: ByteArray, ocrDataFront: OcrResponseFront,sharedViewModel: SharedViewModel,referenceNumber:String) {

                  // Log the ByteArray size
            Log.d("navigateToNewActivity", "ByteArray size: ${byteArray}")

            // Log the OCR data
            Log.d("navigateToNewActivity", "OCR Data: ${ocrDataFront}")

            // Log the SharedViewModel (optional, for debugging purposes)
            Log.d("navigateToNewActivity", "SharedViewModel: ${sharedViewModel}")
                    val intent = Intent(currentActivity, NewActivity::class.java)
                    intent.putExtra("imageByteArray", byteArray) // Pass ByteArray instead of Bitmap
                    intent.putExtra("ocrProcessingData", ocrDataFront) // Pass the ocrProcessingData
                    intent.putExtra("referenceNumber", referenceNumber) // Pass the reference number (optional)
                    currentActivity?.startActivity(intent)
        }
}


class NewActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel

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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }
        contentContainer.addView(imageView)

        // Your existing image processing code
        val byteArray = intent.getByteArrayExtra("imageByteArray")
        Log.d("FrontImage", "ByteArray size: ${byteArray?.size} bytes")
        byteArray?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            Log.d("FrontImage", "Bitmap decoded: ${bitmap != null}")
            if (bitmap != null) {
                val rotatedBitmap = rotateImage(bitmap, 0f)
                imageView.viewTreeObserver.addOnGlobalLayoutListener {
                    val width = imageView.width
                    val height = (width * 3) / 4
                    val layoutParams = imageView.layoutParams
                    layoutParams.width = width
                    layoutParams.height = height
                    imageView.layoutParams = layoutParams
                    imageView.setImageBitmap(rotatedBitmap)
                }
            }
        }

        // OCR Data processing
        val ocrProcessingData = intent.getSerializableExtra("ocrProcessingData") as? OcrResponseFront
        ocrProcessingData?.let { ocrData ->
            val ocrTextLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.START
                setPadding(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
            }

            // Add OCR data TextViews with improved styling
            val textViews = listOf(
                "Full Name: ${ocrData.fullName}",
                "Date of Birth: ${ocrData.dateOfBirth}",
                "Sex: ${ocrData.sex}",
                "Nationality: ${ocrData.nationality}",
                "FCN: ${ocrData.fcn}"
            ).map { text ->
                TextView(this).apply {
                    setText(text)
                    setTextColor(Color.parseColor("#333333"))
                    textSize = 16f
                    setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
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
                processBackIdCard(byteArray, ocrProcessingData,referenceNumber)
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


     private fun processBackIdCard(byteArray: ByteArray?, ocrProcessingData: OcrResponseFront?,referenceNumber: String?) {
        val intent = Intent(this, BackIdCardActivity::class.java).apply {
            putExtra("imageByteArray", byteArray)
            putExtra("ocrProcessingData", ocrProcessingData)
            putExtra("referenceNumber", referenceNumber)
        }
        startActivity(intent)
    }
}




class BackIdCardActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private var reactContext: ReactContext? = null
    private lateinit var progressBar: ProgressBar
    private var promise: Promise? = null
    private lateinit var captureButton: Button
    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            text = "Take a Picture of Back side of ID Card"
            textSize = 22f

            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                900,
                250
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

            // //Navigation
            // captureButton.setOnClickListener {
            //   takePhoto(promise,context)
            // }


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


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
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

    private fun takePhoto(viewModel: SharedViewModel,referenceNumber: String) {
        val imageCapture = imageCapture ?: run {
            Log.e("CaptureBack", "ImageCapture is null. Cannot proceed with photo capture.")
            return
        }

        Log.d("CaptureBack", "Starting photo capture process...")
        progressBar.visibility = View.VISIBLE

        val tempFile = File(externalCacheDir, "temp_image.jpg").also {
            Log.d("CaptureBack", "Temporary file created at: ${it.absolutePath}")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        Log.d("CaptureBack", "Initiating photo capture...")
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CaptureBack", "Photo captured successfully. Stopping camera preview...")

                    // Stop the camera preview
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(this@BackIdCardActivity)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll() // Unbind the camera preview
                        Log.d("CaptureBack", "Camera preview stopped.")
                    }, ContextCompat.getMainExecutor(this@BackIdCardActivity))

                    // Process the captured image
                    Log.d("CaptureBack", "Reading captured image into byte array...")
                    val byteArray = tempFile.readBytes().also {
                        Log.d("CaptureBack", "Image read successfully. Size: ${it.size} bytes")
                    }

                    Log.d("CaptureBack", "Sending image to API...")
                    sendImageToApi(byteArray, viewModel,referenceNumber)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CaptureBack", "Photo capture failed: ${exception.message}", exception)
                    handleError("Photo capture failed", exception)
                }
            }
        )
    }


    private fun sendImageToApi(byteArray: ByteArray, viewModel: SharedViewModel,referenceNumber: String) {
        Log.d("sendImageToApi", "Received byte array of size: ${byteArray.size} bytes")

        // Show loading dialog
        showLoadingDialog()

        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .build()

        val mediaType = "image/jpeg".toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", byteArray.toRequestBody(mediaType))
            .build()

        val croppingRequest = Request.Builder()
            .url("https://api.innovitegrasuite.online/crop-aadhar-card/")
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val croppingResponse = client.newCall(croppingRequest).execute()

                if (croppingResponse.isSuccessful) {
                    val croppedImageData = croppingResponse.body?.bytes()

                    if (croppedImageData != null) {
                        processOcrRequest(croppedImageData, client, mediaType, viewModel,referenceNumber)
                    } else {
                        throw Exception("Failed to get cropped image data")
                    }
                } else {
                    throw Exception("Failed to crop image (Error ${croppingResponse.code})")
                }
            } catch (e: Exception) {
                handleApiError(e)
            }
        }
    }

    private suspend fun processOcrRequest(
        croppedImageData: ByteArray,
        client: OkHttpClient,
        mediaType: MediaType,
        viewModel: SharedViewModel,
        referenceNumber: String
    ) {
      Log.d("referenceNumber", "${referenceNumber}")

        val ocrRequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", croppedImageData.toRequestBody(mediaType))
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
                handleSuccessfulOcrResponse(ocrResponse, croppedImageData, viewModel,referenceNumber)
            } else {
                throw Exception("OCR processing failed (Error ${ocrResponse.code})")
            }
        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private suspend fun handleSuccessfulOcrResponse(
        ocrResponse: Response,
        croppedImageData: ByteArray,
        viewModel: SharedViewModel,
        referenceNumber: String
    ) {
        Log.d("OCRResponse", "handleSuccessfulOcrResponse${ocrResponse}")
        try {

            // Get front data from ViewModel
            val frontImageBitmap = viewModel.frontImage.value
            val frontOcrData = viewModel.ocrData.value

            Log.d("ViewModelBackId", "Front Image Data: $frontImageBitmap")
            Log.d("ViewModelBackId", "Front Ocr Data: $frontOcrData")


            val responseJson = ocrResponse.body?.string()
            Log.d("OCRResponse", "OCR Response: $responseJson")

            val jsonObject = JSONObject(responseJson ?: "")
            val dataObject = jsonObject.getJSONObject("id_analysis")
            val backData = dataObject.getJSONObject("back")

            val ocrDataBack = OcrResponseBack(
                Date_of_Expiry = backData.optString("Date_of_Expiry", " "),
                Phone_Number = backData.optString("Phone_Number", " "),
                Region_City_Admin = backData.optString("Region_City_Admin", " "),
                Zone_City_Admin_Sub_City = backData.optString("Zone_City_Admin_Sub_City", " "),
                Woreda_City_Admin_Kebele = backData.optString("Woreda_City_Admin_Kebele", " "),
                FIN = backData.optString("FIN", " ")
            )

            val bitmap = BitmapFactory.decodeByteArray(croppedImageData, 0, croppedImageData.size)

            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                viewModel.setBackImage(bitmap)
                viewModel.setOcrData2(ocrDataBack)
            // Convert front bitmap to byteArray
            val frontByteArray = frontImageBitmap?.let { bitmap ->
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
            }
                // Navigate with both front and back data
                navigateToBackActivity(
                    byteArrayBack = croppedImageData,
                    ocrDataBack = ocrDataBack,
                    byteArrayFront = frontByteArray,
                    ocrDataFront = frontOcrData,
                    referenceNumber
                )
            }
        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private suspend fun handleApiError(error: Exception) {
        withContext(Dispatchers.Main) {
            hideLoadingDialog()
            showErrorDialog(error)
        }
    }

    private fun showLoadingDialog() {
        progressBar ?.visibility = View.VISIBLE
        // Disable any buttons or interactions while loading
        captureButton ?.isEnabled = false
    }

    private fun hideLoadingDialog() {
        progressBar?.visibility = View.GONE
        // Re-enable buttons
        captureButton ?.isEnabled = true
    }

    private fun showErrorDialog(error: Exception) {
        val errorMessage = when {
            error.message?.contains("crop", ignoreCase = true) == true ->
                "Failed to crop the ID card image. Please ensure the entire card is visible and try again."
            error.message?.contains("OCR", ignoreCase = true) == true ->
                "Failed to read the ID card. Please ensure the image is clear and try again."
            else -> "An error occurred: ${error.message}"
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


    // Method to navigate to a new Android Activity
    private fun navigateToBackActivity(byteArrayBack: ByteArray,
        ocrDataBack: OcrResponseBack,
        byteArrayFront: ByteArray?,
        ocrDataFront: OcrResponseFront?,
        referenceNumber: String) {
          Log.d("navigateToBackActivity", "ByteArray size: ${byteArrayBack}")
            val intent = Intent(this, BackActivity::class.java)
            intent.putExtra("imageByteArray", byteArrayBack) // Pass ByteArray instead of Bitmap
            intent.putExtra("ocrProcessingData", ocrDataBack) // Pass the ocrProcessingData
            intent.putExtra("frontByteArray", byteArrayFront) // Pass ByteArray instead of Bitmap
            intent.putExtra("frontOcrData", ocrDataFront) //pass the frontOcrData
            intent.putExtra("referenceNumber", referenceNumber) // pass the referenceNumber
            startActivity(intent)
            finish()
      }


    private fun handleError(message: String, error: Exception? = null) {
        if (isInitialized()) {
            promise?.reject("ERROR", message, error)
        } else {
            Log.e(TAG, "Error: $message", error)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}



class BackActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String? = null

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
            intent.getByteArrayExtra("frontByteArray")?.let { byteArray ->
                val originalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                // Rotate the bitmap by 90 degrees
                val matrix = Matrix().apply {
                    postRotate(0f) // Rotate 0 degrees clockwise
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
                )

                // Set the rotated bitmap to the ImageView
                frontImageView.setImageBitmap(rotatedBitmap)

                // Update the shared view model with the rotated bitmap
                sharedViewModel.setFrontImage(rotatedBitmap)
            }

            // Back Image
            intent.getByteArrayExtra("imageByteArray")?.let { byteArray ->
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                backImageView.setImageBitmap(bitmap)
                sharedViewModel.setBackImage(bitmap)
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

                // Display Back OCR Data
                addDataRow(backOcrLayout, "Date of Expiry", data.Date_of_Expiry)
                addDataRow(backOcrLayout, "Phone Number", data.Phone_Number)
                addDataRow(backOcrLayout, "Region/City/Admin", data.Region_City_Admin)
                addDataRow(backOcrLayout, "Zone/City/Admin/Sub-City", data.Zone_City_Admin_Sub_City)
                addDataRow(backOcrLayout, "Woreda/City/Admin/Kebele", data.Woreda_City_Admin_Kebele)
                addDataRow(backOcrLayout, "FIN", data.FIN)
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
                400.dpToPx()
            ).apply {
                setMargins(0, 8.dpToPx(), 0, 16.dpToPx())
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
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
            setStroke(1.dpToPx(), Color.parseColor("#E0E0E0"))
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
                intent.putExtra("frontByteArray", frontByteArray)
                intent.putExtra("imageByteArray", backByteArray) // back image
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


class Liveliness : AppCompatActivity() {
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

    private var headMovementTasks = mutableMapOf(
        "Blink detected" to false,
        "Head moved right" to false,
        "Head moved left" to false
    )

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        referenceNumber = intent.getStringExtra("referenceNumber")

        super.onCreate(savedInstanceState)
        setupUI()
        initializeViewModel()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }
    }

    private fun initializeViewModel() {
        sharedViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SharedViewModel::class.java]

        try {
            // Process front image
            intent.getByteArrayExtra("frontByteArray")?.let { byteArrayFront ->
                val bitmap = BitmapFactory.decodeByteArray(byteArrayFront, 0, byteArrayFront.size)
                bitmap?.let {
                    sharedViewModel.setFrontImage(it)
                    Log.d("LivelinessData", "Front image set in ViewModel")
                }
            }

            // Process back image
            intent.getByteArrayExtra("imageByteArray")?.let { byteArrayBack ->
                val bitmap = BitmapFactory.decodeByteArray(byteArrayBack, 0, byteArrayBack.size)
                bitmap?.let {
                    sharedViewModel.setBackImage(it)
                    Log.d("LivelinessData", "Back image set in ViewModel")
                }
            }

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
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isDetectingFaces &&
            System.currentTimeMillis() - lastDetectionTime >= detectionInterval
        ) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            isDetectingFaces = true
            lastDetectionTime = System.currentTimeMillis()

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
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

        if (faces.isEmpty()) {
            runOnUiThread {
                overlayImageView.setImageBitmap(null)
            }
            return
        }

        for (face in faces) {
            // Modify the existing boundingBox
            val bounds = face.boundingBox
            // Increase width and height by 20%
            val widthIncrease = (bounds.width() * 0.2).toInt()
            val heightIncrease = (bounds.height() * 0.2).toInt()

            // Adjust the bounds directly
            bounds.left -= widthIncrease / 2
            bounds.right += widthIncrease / 2
            bounds.top -= heightIncrease / 2
            bounds.bottom += heightIncrease / 2

            paint.color = Color.GREEN
            canvas.drawRect(
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.right.toFloat(),
                bounds.bottom.toFloat(),
                paint
            )
        }

        runOnUiThread {
            overlayImageView.setImageBitmap(mutableBitmap)
        }

        if (!isPictureTaken && headMovementTasks.all { it.value }) {
            takePicture()
        }
    } catch (e: Exception) {
        Log.e("FaceOverlay", "Error drawing face overlay: ${e.message}")
    }
}

        private fun processDetectedFace(face: Face) {
            val headEulerAngleY = face.headEulerAngleY
            val leftEyeOpenProb = face.leftEyeOpenProbability ?: -1.0f
            val rightEyeOpenProb = face.rightEyeOpenProbability ?: -1.0f

            when {
                !headMovementTasks["Blink detected"]!! &&
                        leftEyeOpenProb < 0.5 && rightEyeOpenProb < 0.5 -> {
                    updateTask("Blink detected")
                    showInstructionText("Please turn your head to the left")
                    Log.d("FaceDetection", "Blink detected")
                }
                headMovementTasks["Blink detected"]!! &&
                        !headMovementTasks["Head moved right"]!! &&
                        headEulerAngleY > 10 -> {
                    updateTask("Head moved right")
                    showInstructionText("Please turn your head to the right")
                    Log.d("FaceDetection", "Head turned right")
                }
                headMovementTasks["Head moved right"]!! &&
                        !headMovementTasks["Head moved left"]!! &&
                        headEulerAngleY < -10 -> {
                    updateTask("Head moved left")
                    showInstructionText("Perfect! Taking your photo...")
                    Log.d("FaceDetection", "Head turned left")
                    if (!isPictureTaken) {
                        takePicture()
                    }
                }
            }

            if (!headMovementTasks["Blink detected"]!!) {
                showInstructionText("Please close your eyes for a second")
            }
        }




        private fun takePicture() {
        isPictureTaken = true
        val imageCapture = imageCapture ?: return

        showCountdownUI {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = image.toBitmap()
                            val byteArray = bitmap.toByteArray()
                            image.close()

                            CoroutineScope(Dispatchers.IO).launch {
                                matchFaces(byteArray)
                            }
                        } catch (e: Exception) {
                            Log.e("Capture", "Failed to process captured image", e)
                            showErrorDialog("Failed to process captured image: ${e.message}")
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("Capture", "Image capture failed", exception)
                        showErrorDialog("Failed to capture image: ${exception.message}")
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
                    300, 200
                ).apply {
                    gravity = Gravity.CENTER
                }
                textSize = 48f
                setTextColor(Color.WHITE)
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

   private suspend fun matchFaces(selfieBytes: ByteArray) {
    withContext(Dispatchers.Main) {
        try {
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

            // Rotate the selfie image before sending
            val rotatedSelfieBytes = rotateImage(selfieBytes, 270)  // Example: 270 degrees rotation

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
            handleMatchingResponse(response, rotatedSelfieBytes)

        } catch (e: Exception) {
            Log.e("FaceMatching", "Error during face matching: ${e.message}", e)
            hideLoadingDialog()
            handleAnyError("Face matching failed: ${e.message}")
        }
    }
}

// Rotate the image by a specified angle (in degrees)
private fun rotateImage(imageBytes: ByteArray, angle: Int): ByteArray {
    // Decode byte array to Bitmap
    val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Create a matrix to rotate the image
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())  // Convert angle to Float

    // Create a new Bitmap from the original Bitmap, rotated by the specified angle
    val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

    // Convert rotated Bitmap back to ByteArray
    val byteArrayOutputStream = ByteArrayOutputStream()
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

private fun handleMatchingResponse(response: Response, selfieBytes: ByteArray) {
    try {
        // Log raw response
        val responseBody = response.body?.string()
        Log.d("FaceMatching", "Response code: ${response.code}")
        Log.d("FaceMatching", "Response body: $responseBody")

        if (!response.isSuccessful) {
            throw Exception("Server returned error code: ${response.code}")
        }

        if (responseBody == null) {
            throw Exception("Empty response from server")
        }

        val jsonObject = JSONObject(responseBody)
        Log.d("FaceMatching", "Parsed JSON response: $jsonObject")

        val success = jsonObject.optBoolean("success", false)
        val verificationStatus = jsonObject.optString("verification_status")

        Log.d("FaceMatching", "Success: $success, Status: $verificationStatus")

        if (!success) {
            val errorMessage = jsonObject.optString("message", "Unknown error")
            throw Exception("Verification failed: $errorMessage")
        }

        if (verificationStatus != "Success") {
            throw Exception("Verification status: $verificationStatus")
        }

        // Prepare verification data
        val verificationData = prepareVerificationData(selfieBytes)
        Log.d("FaceMatching", "Verification successful, preparing data")

        // Navigate to success screen
        hideLoadingDialog()
        sendVerificationSuccess(
            selfieBytes,
            verificationData["frontImage"] as ByteArray,
            verificationData["backImage"] as ByteArray,
            sharedViewModel.ocrData.value,
            sharedViewModel.ocrData2.value
        )
    } catch (e: Exception) {
        Log.e("FaceMatching", "Error handling response: ${e.message}", e)
        throw e
    }
}



    private fun prepareVerificationData(selfieBytes: ByteArray): Map<String, Any> {
        val verificationData = mutableMapOf<String, Any>()

        // Add images
        sharedViewModel.frontImage.value?.let { bitmap ->
            verificationData["frontImage"] = bitmap.toByteArray()
        }
        sharedViewModel.backImage.value?.let { bitmap ->
            verificationData["backImage"] = bitmap.toByteArray()
        }
        verificationData["selfieImage"] = selfieBytes

        // Add OCR data
        sharedViewModel.ocrData.value?.let { frontData ->
            verificationData["ocrDataFront"] = frontData.toMap()
        }
        sharedViewModel.ocrData2.value?.let { backData ->
            verificationData["ocrDataBack"] = backData.toMap()
        }

        return verificationData
    }

    // Utility Functions
    private fun Bitmap.toByteArray(): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.toByteArray()
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
                textSize = 22f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(900, 250).apply {
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

    private fun sendVerificationSuccess(
        selfieBytes: ByteArray,
        frontBytes: ByteArray,
        backBytes: ByteArray,
        ocrDataFront: OcrResponseFront?,
        ocrDataBack: OcrResponseBack?
    ) {
        // try {
        //     val intent = Intent(this, ReactNativeActivity::class.java).apply {
        //         putExtra("selfieImage", selfieBytes)
        //         putExtra("frontImage", frontBytes)
        //         putExtra("backImage", backBytes)
        //         putExtra("frontOcr", ocrDataFront)
        //         putExtra("backOcr", ocrDataBack)
        //     }
        //     startActivity(intent)
        //     finish()
        // } catch (e: Exception) {
        //     Log.e("VerificationError", "Error starting ReactNativeActivity", e)
        //     handleAnyError("Failed to complete verification: ${e.message}")
        // }
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

    // Lifecycle Methods
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}





class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val _frontImage = MutableStateFlow<Bitmap?>(null)
    private val _backImage = MutableStateFlow<Bitmap?>(null)
    private val _ocrData = MutableStateFlow<OcrResponseFront?>(null)
    private val _ocrData2 = MutableStateFlow<OcrResponseBack?>(null)
    private val _errorState = MutableStateFlow<String?>(null)

    val frontImage: StateFlow<Bitmap?> get() = _frontImage
    val backImage: StateFlow<Bitmap?> get() = _backImage
    val ocrData: StateFlow<OcrResponseFront?> get() = _ocrData
    val ocrData2: StateFlow<OcrResponseBack?> get() = _ocrData2
    val errorState: StateFlow<String?> get() = _errorState

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
     // Function to set error
    fun setError(message: String) {
        Log.e("ViewModel", "Error occurred: $message")
        _errorState.value = message
    }

    //Function to clear error
    fun clearError() {
        _errorState.value = null
    }
}



data class OcrResponseFront(
    val fullName: String,
    val dateOfBirth: String,
    val sex: String,
    val nationality: String,
    val fcn: String,
    val croppedFace: String?
) : Serializable


data class OcrResponseBack(
        val Date_of_Expiry: String,
        val Phone_Number: String,
        val Region_City_Admin: String,
        val Zone_City_Admin_Sub_City: String,
        val Woreda_City_Admin_Kebele: String,
        val FIN: String
) : Serializable



fun OcrResponseFront.toMap(): Map<String, Any> {
    return mapOf(
        "fullName" to (fullName ?: ""),
        "dateOfBirth" to (dateOfBirth ?: ""),
        "sex" to (sex ?: ""),
        "nationality" to (nationality ?: ""),
        "fcn" to (fcn ?: ""),
        "croppedFace" to (croppedFace ?: "")
    )
}

fun OcrResponseBack.toMap(): Map<String, Any> {
    return mapOf(
        "dateOfExpiry" to (Date_of_Expiry ?: "Not visible"),
        "phoneNumber" to (Phone_Number ?: "Not visible"),
        "regionCityAdmin" to (Region_City_Admin ?: "Not visible"),
        "zoneCityAdminSubCity" to (Zone_City_Admin_Sub_City ?: "Not visible"),
        "woredaCityAdminKebele" to (Woreda_City_Admin_Kebele ?: "Not visible"),
        "fin" to (FIN ?: "")
    )
}
