package com.inno

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.view.View
import android.widget.LinearLayout.LayoutParams

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
        Log.d("OCRData", "Received OCR Data: $digitalFront")
        Log.d("OCRData", "Received OCR Data: $digitalBack")

        setupUI(digitalFront,digitalBack, referenceNumber)
    }

    private fun setupUI(digitalFront: OcrResponseFront?, digitalBack: OcrResponseBack?, referenceNumber: String?) {
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

        // Title
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

        // ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        // Container for both cards
        val cardsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
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

        cardsContainer.addView(frontResultsCard)

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
            addResultRow(backResultsCard, "Phone Number", data.Phone_Number)
            addResultRow(backResultsCard, "Region/City Admin", data.Region_City_Admin)
            addResultRow(backResultsCard, "Zone/City Admin/Sub City", data.Zone_City_Admin_Sub_City)
            addResultRow(backResultsCard, "Woreda/City Admin/Kebele", data.Woreda_City_Admin_Kebele)
        }

        cardsContainer.addView(backResultsCard)
        scrollView.addView(cardsContainer)
        mainLayout.addView(scrollView)

        // Continue Button
        continueButton = Button(this).apply {
            text = "Proceed to Liveliness Detection"
            setBackgroundColor(Color.parseColor("#221BC7"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                150
            ).apply {
                topMargin = 16
            }
            setOnClickListener {
                try {
                    val intent = Intent(this@DigitalIDPreviewBackActivity, Liveliness::class.java)
                    intent.putExtra("frontOcrData", digitalFront)
                    intent.putExtra("ocrProcessingData", digitalBack)
                    intent.putExtra("referenceNumber", referenceNumber)
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
        mainLayout.addView(continueButton)

        setContentView(mainLayout)
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
