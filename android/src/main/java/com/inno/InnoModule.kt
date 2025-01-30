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
    fun startCamera(promise: Promise) {
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
            setupUI(activity, promise)
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
            takePicture(promise, sharedViewModel)
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

    private fun takePicture(promise: Promise, sharedViewModel: SharedViewModel) {
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
                            sendImageToApi(byteArray, sharedViewModel, promise)
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
    promise: Promise
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
                        .addFormDataPart("reference_id", "123423423428989")
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
                        handleSuccessfulOcrResponse(ocrResponse, croppedImageData, sharedViewModel,promise)
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
        promise: Promise
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
                navigateToNewActivity(byteArray, ocrDataFront,sharedViewModel)

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
                        setupUI(activity, promise)
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
            val matrix = Matrix().apply { postRotate(90f) }
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
        private fun navigateToNewActivity(byteArray: ByteArray, ocrDataFront: OcrResponseFront,sharedViewModel: SharedViewModel) {
            val intent = Intent(currentActivity, NewActivity::class.java)
            intent.putExtra("imageByteArray", byteArray) // Pass ByteArray instead of Bitmap
            intent.putExtra("ocrProcessingData", ocrDataFront) // Pass the ocrProcessingData
            currentActivity?.startActivity(intent)
        }
      }


class NewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create main container
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Add two simple text views
        TextView(this).apply {
            text = "Welcome to New Activity"
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }.also { mainContainer.addView(it) }

        TextView(this).apply {
            text = "This is a sample text"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }.also { mainContainer.addView(it) }

        setContentView(mainContainer)
        Log.d("NewActivity", "Activity created")
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
