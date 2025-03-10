package com.inno

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.util.Log
import com.facebook.react.bridge.Callback

@ReactModule(name = TimeoutEventModule.NAME)
class TimeoutEventModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "TimeoutEventModule"
        const val EVENT_NAME = "onTimeoutEvent"
    }

    override fun getName(): String {
        return NAME
    }

    // Required override to register the events this module can emit
    override fun getConstants(): Map<String, Any> {
        return mapOf(
            "EVENT_NAME" to EVENT_NAME
        )
    }

    @ReactMethod
    fun addListener(eventName: String) {
        // Required for RN built in Event Emitter
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Required for RN built in Event Emitter
    }

    fun emitTimeoutEvent(status: Int, message: String?) {
        try {
            if (!reactApplicationContext.hasActiveCatalystInstance()) {
                Log.e(NAME, "React context not active")
                return
            }

            val params: WritableMap = com.facebook.react.bridge.Arguments.createMap().apply {
                putInt("timeoutStatus", status)
                putString("timeoutMessage", message)
            }
            
            Log.d(NAME, "Emitting timeout event: status=$status, message=$message")
            
            // Get the event emitter and emit the event
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(EVENT_NAME, params)
            
            Log.d(NAME, "Successfully emitted timeout event")
        } catch (e: Exception) {
            Log.e(NAME, "Failed to emit timeout event: ${e.message}")
            e.printStackTrace()
        }
    }
}