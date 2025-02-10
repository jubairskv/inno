package com.inno

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

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

        val textView = TextView(this).apply {
            text = "Hello from Selection Activity!"
            textSize = 24f
        }

        setContentView(textView)
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
