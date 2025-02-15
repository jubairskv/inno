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
import androidx.cardview.widget.CardView
import com.facebook.react.bridge.*
import android.app.Activity
import com.facebook.react.bridge.UiThreadUtil
import java.text.SimpleDateFormat
import java.util.*

class SelectionActivity(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val TAG = "SelectionActivity"
    }

    override fun getName(): String = "SelectionActivity"

    private fun generateReferenceNumber(): String {
        try {
            val currentDate = Date()
            val dateFormatter = SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault())
            val formattedDateTime = dateFormatter.format(currentDate)
            val randomNumber = String.format("%03d", (0..999).random())
            var referenceId = "$formattedDateTime$randomNumber"

            if (referenceId.length > 32) {
                referenceId = referenceId.substring(0, 32)
            }

            Log.d(TAG, "Generated reference number: $referenceId")
            return "INNOVERIFYJUB$referenceId"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate reference number: ${e.message}")
            return "INNOVERIFYJUB${System.currentTimeMillis()}" // Fallback reference number
        }
    }

    @ReactMethod
    fun openSelectionUI(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                val activity = currentActivity ?: throw Exception("Activity is null")

                // Create main container
                val mainLayout = LinearLayout(activity).apply {
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
                mainLayout.addView(TextView(activity).apply {
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
                mainLayout.addView(TextView(activity).apply {
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
                val spacer = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                    ).apply {
                        weight = 1f // This will take up all remaining space
                    }
                }
                mainLayout.addView(spacer)

                // Physical ID Card Button
                val physicalIdCard = CardView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        400
                    ).apply {
                        bottomMargin =16
                    }
                    radius = 24f
                    setCardBackgroundColor(Color.parseColor("#221BC7"))
                    setOnClickListener {
                        activity.runOnUiThread {
                            try {
                                val referenceNumber = generateReferenceNumber()
                                val intent = Intent(activity, FrontIdCardActivity::class.java)
                                intent.putExtra("REFERENCE_NUMBER", referenceNumber)
                                intent.putExtra("START_CAMERA", true)
                                activity.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to start camera: ${e.message}")
                                promise.reject("CAMERA_ERROR", "Failed to start camera: ${e.message}")
                            }
                        }
                    }
                }

                // Physical ID Card Content
                val physicalIdContent = LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }.also { content ->
                    content.addView(TextView(activity).apply {
                        text = "Physical ID"
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                    })
                    content.addView(TextView(activity).apply {
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
                val digitalIdCard = CardView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        400
                    )
                    radius = 24f
                    setCardBackgroundColor(Color.parseColor("#221BC7"))
                    setOnClickListener {
                        activity.runOnUiThread {
                            try {
                                val referenceNumber = generateReferenceNumber()
                                val intent = Intent(activity, DigitalIDFrontActivity::class.java)
                                intent.putExtra("REFERENCE_NUMBER", referenceNumber)
                                activity.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to start DigitalIDFrontActivity: ${e.message}")
                                promise.reject("NAVIGATION_ERROR", "Failed to start DigitalIDFrontActivity: ${e.message}")
                            }
                        }
                    }
                }

                // Digital ID Card Content
                val digitalIdContent = LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }.also { content ->
                    content.addView(TextView(activity).apply {
                        text = "Digital ID"
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                    })
                    content.addView(TextView(activity).apply {
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
                activity.setContentView(mainLayout)
                promise.resolve("Selection screen opened successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to open selection screen: ${e.message}")
                promise.reject("ERROR_OPENING_SCREEN", "Failed to open selection screen: ${e.message}")
            }
        }
    }

}
