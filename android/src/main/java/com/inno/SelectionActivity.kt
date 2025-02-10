package com.inno

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import android.util.Log

class SelectionActivity : ComponentActivity() {
    companion object {
        const val TAG = "SelectionActivity"
        private var activityClosedCallback: (() -> Unit)? = null
        const val EXTRA_REFERENCE_NUMBER = "REFERENCE_NUMBER"

        fun setActivityClosedCallback(callback: () -> Unit) {
            activityClosedCallback = callback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val referenceNum = intent.getStringExtra("REFERENCE_NUMBER")

         Log.d("SelectionActivity", "REF: $referenceNum")


        // Create main container
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)

            // Set gradient background
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#D1F1FF"), Color.parseColor("#FFFFFF"))
            )
            background = gradient
        }

        // Add title
        val titleText = TextView(this).apply {
            text = "Select ID Card Type"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }
        mainLayout.addView(titleText)

        // Add subtitle
        val subtitleText = TextView(this).apply {
            text = "Choose how you want to proceed with your eKYC verification"
            textSize = 16f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48
            }
        }
        mainLayout.addView(subtitleText)

        // Physical ID Card Button
        val physicalIdCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ).apply {
                bottomMargin = 32
            }
            radius = 24f
            setCardBackgroundColor(Color.parseColor("#221BC7"))

            setOnClickListener {
                try {
                    val intent = Intent(this@SelectionActivity, BackIdCardActivity::class.java)
                    intent.putExtra("REFERENCE_NUMBER", referenceNum)
                    intent.putExtra("START_CAMERA", true)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start camera: ${e.message}")
                }
            }
        }

        // Physical ID Card Content
        val physicalIdContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val physicalIdText = TextView(this).apply {
            text = "Physical ID"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val physicalIdSubtext = TextView(this).apply {
            text = "Capture physical card image"
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.8f
            gravity = Gravity.CENTER
        }

        physicalIdContent.addView(physicalIdText)
        physicalIdContent.addView(physicalIdSubtext)
        physicalIdCard.addView(physicalIdContent)
        mainLayout.addView(physicalIdCard)

        // Digital ID Card Button
        val digitalIdCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            radius = 24f
            setCardBackgroundColor(Color.parseColor("#221BC7"))

            // setOnClickListener {
            //     val intent = Intent(this@SelectionActivity, DigitalIDFrontActivity::class.java)
            //     intent.putExtra("REFERENCE_NUMBER", referenceNum)
            //     startActivity(intent)
            //     finish()
            // }
        }

        // Digital ID Card Content
        val digitalIdContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val digitalIdText = TextView(this).apply {
            text = "Digital ID"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val digitalIdSubtext = TextView(this).apply {
            text = "Upload digital card image"
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.8f
            gravity = Gravity.CENTER
        }

        digitalIdContent.addView(digitalIdText)
        digitalIdContent.addView(digitalIdSubtext)
        digitalIdCard.addView(digitalIdContent)
        mainLayout.addView(digitalIdCard)

        setContentView(mainLayout)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activityClosedCallback?.invoke()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityClosedCallback?.invoke()
    }
}
