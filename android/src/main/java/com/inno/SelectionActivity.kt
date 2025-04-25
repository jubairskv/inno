package com.inno

import android.content.Intent
import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SelectionActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SelectionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SelectionActivity created")
        setupUI()
    }

    private fun setupUI() {
        val referenceNumber = intent.getStringExtra("REFERENCE_NUMBER") ?: ""
        val apkName = intent.getStringExtra("APK_NAME") ?: ""
        Log.d(TAG, "Setting up UI with reference: $referenceNumber, apkName: $apkName")

        // Create main container
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#D1F1FF"), Color.parseColor("#FFFFFF"))
            )
        }

        // Add title
        mainLayout.addView(TextView(this).apply {
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
        })

        // Add subtitle
        mainLayout.addView(TextView(this).apply {
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
        })

        // Spacer to push buttons to the bottom
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
            }
        }
        mainLayout.addView(spacer)

        // Physical ID Card Button
        val physicalIdCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ).apply {
                bottomMargin = 16
            }
            radius = 24f
            setCardBackgroundColor(Color.parseColor("#221BC7"))
            setOnClickListener {
                try {
                    val intent = Intent(this@SelectionActivity, FrontIdCardActivity::class.java)
                    intent.putExtra("REFERENCE_NUMBER", referenceNumber)
                    intent.putExtra("APK_NAME", apkName)
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
        }.also { content ->
            content.addView(TextView(this).apply {
                text = "Physical ID"
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
            content.addView(TextView(this).apply {
                text = "Capture physical card image"
                textSize = 16f
                setTextColor(Color.WHITE)
                alpha = 0.8f
                gravity = Gravity.CENTER
            })
        }
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
            setOnClickListener {
                try {
                    val intent = Intent(this@SelectionActivity, DigitalIDFrontActivity::class.java)
                    intent.putExtra("REFERENCE_NUMBER", referenceNumber)
                    intent.putExtra("APK_NAME", apkName)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start DigitalIDFrontActivity: ${e.message}")
                }
            }
        }

        // Digital ID Card Content
        val digitalIdContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }.also { content ->
            content.addView(TextView(this).apply {
                text = "Digital ID"
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
            content.addView(TextView(this).apply {
                text = "Upload digital card image"
                textSize = 16f
                setTextColor(Color.WHITE)
                alpha = 0.8f
                gravity = Gravity.CENTER
            })
        }
        digitalIdCard.addView(digitalIdContent)
        mainLayout.addView(digitalIdCard)

        // Set the content view
        setContentView(mainLayout)
    }
    
}


