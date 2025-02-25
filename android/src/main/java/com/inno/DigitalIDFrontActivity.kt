package com.inno

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import android.content.res.ColorStateList
import android.widget.FrameLayout
import android.graphics.Typeface

class DigitalIDFrontActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var mainLayout: LinearLayout
    private lateinit var progressContainer: LinearLayout
    private var selectedImageUri: Uri? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String = ""
    private lateinit var imageViewContainer: FrameLayout
    private lateinit var imageViewDimOverlay: View
    private lateinit var dimOverlay: View
    private lateinit var placeholderTextView: TextView

    companion object {
        private const val TAG = "DigitalIDFrontActivity"
        private var activityClosedCallback: (() -> Unit)? = null
        private const val PICK_IMAGE_REQUEST = 1

        fun setActivityClosedCallback(callback: () -> Unit) {
            activityClosedCallback = callback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        referenceNumber = intent.getStringExtra("REFERENCE_NUMBER") ?: ""
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        setupUI(referenceNumber)
    }

    private fun setupUI(referenceNumber: String) {
        // Create main container
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#D1F1FF"), Color.parseColor("#FFFFFF"))
            )
        }

        // Title
        val titleText = TextView(this).apply {
            text = "Upload Digital ID (Front Side)"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }
        mainLayout.addView(titleText)

        // Create image view container
        imageViewContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
                bottomMargin = 16
            }
        }

        // Add image view to container
        imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // Create dim overlay for image view
        imageViewDimOverlay = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            alpha = 0.5f
            visibility = View.GONE
        }

        // Add views to image container
        imageViewContainer.addView(imageView)
        imageViewContainer.addView(imageViewDimOverlay)

        // Add image container to main layout instead of directly adding imageView
        mainLayout.addView(imageViewContainer)


        // Placeholder text for when no image is selected
        placeholderTextView = TextView(this).apply {
            text = "No image selected\nTap 'Upload ID' to select an image"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add placeholder text to the image container
        imageViewContainer.addView(placeholderTextView)

        // Upload Button
        uploadButton = Button(this).apply {
            text = "Upload ID"
            setBackgroundColor(Color.parseColor("#59d5ff"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                800,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = 150
                bottomMargin = 16
            }
             background = GradientDrawable().apply {
                setColor(Color.parseColor("#59d5ff"))
                cornerRadius = 30f
            }
            setOnClickListener {
                if (selectedImageUri != null) {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    val imageBytes = inputStream?.readBytes()
                    if (imageBytes != null) {
                        processImage(imageBytes, referenceNumber)
                    }
                } else {
                    openImagePicker()
                }
            }
        }
        mainLayout.addView(uploadButton)

        // Create a container for progress bar and text
        progressContainer = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(130),  // Increased width to 200dp
                dpToPx(100)   // Increased height to 150dp
            ).apply {
                gravity = Gravity.CENTER
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
                setStroke(2, Color.parseColor("#EEEEEE"))
            }
            setPadding(48, 32, 48, 32)
        }

        // Progress Bar
        progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(50),  // Increased progress bar size to 50dp
                dpToPx(50)   // Increased progress bar size to 50dp
            ).apply {
                bottomMargin = 24  // Increased bottom margin
            }
            indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#221BC7"))
        }

        // Processing Text
        val processingText = TextView(this).apply {
            text = "Processing..."
            setTextColor(Color.parseColor("#221BC7"))
            textSize = 16f  // Increased text size
            gravity = Gravity.CENTER
        }

        // Add views to container
        progressContainer.addView(progressBar)
        progressContainer.addView(processingText)

        // Create dim overlay for entire screen
        dimOverlay = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            alpha = 0.5f
            visibility = View.GONE
        }

        // Create root layout and add views in correct order
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add views in order (bottom to top)
        rootLayout.addView(mainLayout)          // Main content at bottom
        rootLayout.addView(dimOverlay)          // Dim overlay above everything except progress
        rootLayout.addView(progressContainer)    // Progress container on top

        // Initially hide the progress container and dim overlay
        progressContainer.visibility = View.GONE
        dimOverlay.visibility = View.GONE

        setContentView(rootLayout)
    }

    private fun openImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
            uploadButton.text = "Continue"
            placeholderTextView.visibility = View.GONE  // Hide the placeholder text
        }
    }

    private fun showLoadingDialog() {
        dimOverlay.visibility = View.VISIBLE
        progressContainer.visibility = View.VISIBLE
        uploadButton.isEnabled = false
    }

    private fun hideLoadingDialog() {
        dimOverlay.visibility = View.GONE
        progressContainer.visibility = View.GONE
        uploadButton.isEnabled = true
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Your existing processImage function remains the same
    private fun processImage(imageData: ByteArray, referenceNumber: String) {
        showLoadingDialog()

        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .build()

        val mediaType = "image/jpeg".toMediaType()
        val credentials = Credentials.basic("test", "test")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                 val ocrRequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "image.jpg",
                        imageData.toRequestBody(mediaType)
                    )
                    .addFormDataPart("reference_id", referenceNumber)
                    .addFormDataPart("side", "front")
                    .build()

                val ocrRequest = Request.Builder()
                    .url("https://api.innovitegrasuite.online/process-id")
                    .addHeader("api-key", "testapikey")
                    .header("Authorization", credentials)
                    .post(ocrRequestBody)
                    .build()

                val ocrResponse = client.newCall(ocrRequest).execute()
                val responseBody = ocrResponse.body?.string()

                if (!ocrResponse.isSuccessful || responseBody == null) {
                    throw Exception("Server returned code ${ocrResponse.code}")
                }

                handleSuccessfulOcrResponse(ocrResponse, responseBody)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    showErrorDialog(when {
                        e is IOException -> "Network error. Please check your internet connection."
                        e is SocketTimeoutException -> "Request timed out. Please try again."
                        else -> e.message ?: "An error occurred. Please try again."
                    })
                }
            }
        }
    }

    private suspend fun handleSuccessfulOcrResponse(response: Response, responseBody: String) {
        Log.d("handleSuccessfulOcrResponse", "Response: $responseBody")
        try {
            val jsonObject = JSONObject(responseBody ?: "")
            val status = jsonObject.optString("status", "")
            if (status != "success" || jsonObject.isNull("id_analysis")) {
                throw Exception("Required fields could not be extracted. Please upload a clearer photo.")
            }

            val dataObject = jsonObject.getJSONObject("id_analysis")
            val frontData = if (dataObject.has("front")) {
                dataObject.getJSONObject("front")
            } else {
                dataObject
            }

            // Extract required fields
            val fcn = frontData.optString("FCN", "N/A")
            val fullName = frontData.optString("Full_name", "N/A")
            val dateOfBirth = frontData.optString("Date_of_birth", "N/A")
            val sex = frontData.optString("Sex", "N/A")
            val nationality = frontData.optString("Nationality", "N/A")
            val croppedFace = jsonObject.optString("cropped_face","N/A")
            val expiryDate = frontData.optString("Expiry_date", "N/A")
            val croppedId = jsonObject.optString("cropped_id", "N/A")

            if (fcn.isBlank() || fullName.isBlank()) {
                throw Exception("Required fields could not be extracted. Please upload a clearer photo.")
            }

            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                val ocrData = OcrResponseFront(
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    sex = sex,
                    nationality = nationality,
                    fcn = fcn,
                    croppedFace = croppedFace,
                    expiryDate = expiryDate,
                    croppedId = croppedId
                )

                Log.d("PassingOCR", "Passing OCR Data to Results Activity: $ocrData")

                val intent = Intent(this@DigitalIDFrontActivity, DigitalIDPreviewFrontActivity::class.java).apply {
                    putExtra("OCR_DATA", ocrData)
                    putExtra("REFERENCE_NUMBER", referenceNumber)
                }
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                showErrorDialog(e.message ?: "An error occurred processing the image")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activityClosedCallback?.invoke()
        finish()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

