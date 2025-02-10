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

class DigitalIDResultsFrontActivity : AppCompatActivity() {
    private lateinit var mainLayout: LinearLayout
    private lateinit var profileImageView: ImageView
    private lateinit var continueButton: Button

    companion object {
        private const val TAG = "DigitalIDResultsFrontActivity"
        private var activityClosedCallback: (() -> Unit)? = null

        fun setActivityClosedCallback(callback: () -> Unit) {
            activityClosedCallback = callback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get OCR data and reference number from intent
        val ocrData = intent.getSerializableExtra("OCR_DATA") as? OcrResponseFront
        val referenceNum = intent.getStringExtra("REFERENCE_NUMBER")
        Log.d(TAG, "Received OCR Data: $ocrData")

        setupUI(ocrData, referenceNum)
    }

    private fun setupUI(ocrData: OcrResponseFront?, referenceNum: String?) {
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
            text = "ID Front Verification Results"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48
            }
        }
        mainLayout.addView(titleText)

        // Profile Image
        profileImageView = ImageView(this).apply {
            layoutParams = LayoutParams(300, 300).apply {
                bottomMargin = 32
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        mainLayout.addView(profileImageView)

        // Load profile image if available
        ocrData?.croppedFace?.let { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(profileImageView)
        } ?: run {
            profileImageView.visibility = View.GONE
        }

        // Results Card
        val resultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
            }
            setPadding(32, 32, 32, 32)
        }

        // Add OCR data rows
        ocrData?.let { data ->
            addResultRow(resultsCard, "Full Name", data.fullName)
            addResultRow(resultsCard, "FCN", data.fcn)
            addResultRow(resultsCard, "Date of Birth", data.dateOfBirth)
            addResultRow(resultsCard, "Sex", data.sex)
            addResultRow(resultsCard, "Nationality", data.nationality)
        } ?: run {
            val noDataText = TextView(this).apply {
                text = "No data available"
                textSize = 16f
                setTextColor(Color.RED)
                gravity = Gravity.CENTER
            }
            resultsCard.addView(noDataText)
            Log.e(TAG, "OCR Data is null")
        }

        mainLayout.addView(resultsCard)

        // Add space between results and button
        Space(this).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }.also { mainLayout.addView(it) }

        // Continue Button
        continueButton = Button(this).apply {
            text = "Upload ID Back"
            setBackgroundColor(Color.parseColor("#221BC7"))
            setTextColor(Color.WHITE)
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                150
            )
            // setOnClickListener {
            //     val intent = Intent(this@DigitalIDResultsFrontActivity, DigitalIDBackActivity::class.java)
            //     intent.putExtra("FRONT_DATA", ocrData)
            //     intent.putExtra("REFERENCE_NUMBER", referenceNum)
            //     startActivity(intent)
            //     finish()
            // }
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
