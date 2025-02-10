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

class DigitalIDFrontActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var mainLayout: LinearLayout
    private var selectedImageUri: Uri? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var referenceNumber: String = ""

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
        setupUI()
    }

    private fun setupUI() {
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

        // Image View
        imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
                bottomMargin = 16
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.LTGRAY)
        }
        mainLayout.addView(imageView)

        // Upload Button
        uploadButton = Button(this).apply {
            text = "Upload ID"
            setBackgroundColor(Color.parseColor("#221BC7"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = 150
                bottomMargin = 16
            }
            setOnClickListener {
                if (selectedImageUri != null) {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    val imageBytes = inputStream?.readBytes()
                    if (imageBytes != null) {
                        processImage(imageBytes, "123456789098765")
                    }
                } else {
                    openImagePicker()
                }
            }
        }
        mainLayout.addView(uploadButton)

        // Progress Bar (Initially GONE)
        progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }
        mainLayout.addView(progressBar)

        setContentView(mainLayout)
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
        }
    }

    private fun showLoadingDialog() {
        progressBar.visibility = View.VISIBLE
        uploadButton.isEnabled = false
    }

    private fun hideLoadingDialog() {
        progressBar.visibility = View.GONE
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
                // ... rest of your processImage implementation ...
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

    override fun onBackPressed() {
        super.onBackPressed()
        activityClosedCallback?.invoke()
        finish()
    }
}
