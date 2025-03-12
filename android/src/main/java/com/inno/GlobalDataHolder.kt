package com.inno

object GlobalDataHolder {
    private var appName: String? = ""
    private val lock = Any()

    fun storeData(data: String) {
        synchronized(lock) {
            appName = data
        }
    }

    fun getData(): String? {
        synchronized(lock) {
            return appName
        }
    }
}