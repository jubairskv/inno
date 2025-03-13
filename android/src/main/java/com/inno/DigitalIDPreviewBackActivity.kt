package com.inno

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout.LayoutParams
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class DigitalIDPreviewBackActivity : AppCompatActivity() {
    private lateinit var mainLayout: LinearLayout
    private lateinit var profileImageView: ImageView
    private lateinit var continueButton: Button

    companion object {
        private const val TAG = "DigitalIDPreviewBackActivity"
        private var activityClosedCallback: (() -> Unit)? = null

        fun setActivityClosedCallback(callback: () -> Unit) {
            activityClosedCallback = callback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get OCR data and reference number from intent
        val digitalFront = intent.getSerializableExtra("FRONT_OCR_DATA") as? OcrResponseFront
        val digitalBack = intent.getSerializableExtra("BACK_OCR_DATA") as? OcrResponseBack
        val referenceNumber = intent.getStringExtra("REFERENCE_NUMBER")
        val apkName = intent.getStringExtra("APK_NAME")
        Log.d("OCRData", "Received OCR Data: $digitalFront")
        Log.d("OCRData", "Received OCR Data: $digitalBack")

        setupUI(digitalFront,digitalBack, referenceNumber,apkName)
    }

    private fun setupUI(digitalFront: OcrResponseFront?, digitalBack: OcrResponseBack?, referenceNumber: String?,apkName: String?) {
        // Create main container
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#D1F1FF"), Color.parseColor("#FFFFFF"))
            )
        }

        // Title (Outside ScrollView)
        val titleText = TextView(this).apply {
            text = "ID Verification Results"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        }
        mainLayout.addView(titleText)

        // ScrollView for all content
        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        // Container for all scrollable content
        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Profile Image
        profileImageView = ImageView(this).apply {
            layoutParams = LayoutParams(300, 300).apply {
                bottomMargin = 32
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        scrollContent.addView(profileImageView)

        // Load profile image if available
       digitalFront?.croppedFace?.let { imageUrl ->
            loadProfileImage(imageUrl)
        } ?: run {
            profileImageView.visibility = View.GONE
        }

        // Front ID Card Results
        val frontResultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
            }
            setPadding(32, 32, 32, 32)
        }

        // Front Card Title
        frontResultsCard.addView(TextView(this).apply {
            text = "Front ID Details"
            textSize = 18f
            setTextColor(Color.parseColor("#221BC7"))
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        })

        // Add Front OCR data rows
        digitalFront?.let { data ->
            addResultRow(frontResultsCard, "Full Name", data.fullName)
            addResultRow(frontResultsCard, "FCN", data.fcn)
            addResultRow(frontResultsCard, "Date of Birth", data.dateOfBirth)
            addResultRow(frontResultsCard, "Sex", data.sex)
            addResultRow(frontResultsCard, "Nationality", data.nationality)
            addResultRow(frontResultsCard, "Expiry Date", data.expiryDate)

        }

        scrollContent.addView(frontResultsCard)

        // Back ID Card Results
        val backResultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
            }
            setPadding(32, 32, 32, 32)
        }

        // Back Card Title
        backResultsCard.addView(TextView(this).apply {
            text = "Back ID Details"
            textSize = 18f
            setTextColor(Color.parseColor("#221BC7"))
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        })

        // Add Back OCR data rows
        digitalBack?.let { data ->
            addResultRow(backResultsCard, "FIN", data.FIN)
            addResultRow(backResultsCard, "Date of Expiry", data.Date_of_Expiry)
            addResultRow(backResultsCard, "Date of Issue", data.Date_of_Issue)
            addResultRow(backResultsCard, "Phone Number", data.Phone_Number)
            addResultRow(backResultsCard, "Region", data.Region)
            addResultRow(backResultsCard, "Zone", data.Zone)
            addResultRow(backResultsCard, "Woreda", data.Woreda)
        }

        scrollContent.addView(backResultsCard)

        // Continue Button (inside ScrollView)
        continueButton = Button(this).apply {
            text = "Proceed to Liveliness"
            setBackgroundColor(Color.parseColor("#59d5ff"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = LayoutParams(
                800,
                150
            ).apply {
                topMargin = 16
                bottomMargin = 16  // Add bottom margin for better spacing
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#59d5ff"))
                cornerRadius = 30f
            }
            setOnClickListener {
                try {
                    val intent = Intent(this@DigitalIDPreviewBackActivity, Liveliness::class.java)
                    intent.putExtra("frontOcrData", digitalFront)
                    intent.putExtra("ocrProcessingData", digitalBack)
                    intent.putExtra("referenceNumber", referenceNumber)
                    intent.putExtra("apkName", apkName)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting LivelinessActivity: ${e.message}")
                    Toast.makeText(
                        this@DigitalIDPreviewBackActivity,
                        "Error proceeding to next step",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        scrollContent.addView(continueButton)

        // Add scrollContent to ScrollView
        scrollView.addView(scrollContent)

        // Add ScrollView to main layout
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }


    private fun loadProfileImage(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

                withContext(Dispatchers.Main) {
                    bitmap?.let {
                        profileImageView.setImageBitmap(it)
                    } ?: run {
                        profileImageView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    profileImageView.visibility = View.GONE
                }
            }
        }
    }

    private fun addResultRow(parent: LinearLayout, label: String, value: String) {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        // Label
        val labelView = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.GRAY)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        rowLayout.addView(labelView)

        // Value
        val valueView = TextView(this).apply {
            text = value.ifEmpty { "N/A" }
            textSize = 16f
            setTextColor(Color.BLACK)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        rowLayout.addView(valueView)

        parent.addView(rowLayout)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activityClosedCallback?.invoke()
        finish()
    }
}
