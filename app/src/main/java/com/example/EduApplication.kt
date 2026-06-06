package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import java.io.File

class EduApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Pre-create WebView cache directories to prevent chromium simple_backend_impl directory creation failure
        try {
            val cachePrefDir1 = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!cachePrefDir1.exists()) {
                val created = cachePrefDir1.mkdirs()
                android.util.Log.d("EduApplication", "Pre-creating WebView cache js directory: $created")
            }
            // Set readable, writeable permissions explicitly
            cachePrefDir1.setReadable(true, false)
            cachePrefDir1.setWritable(true, false)
            cachePrefDir1.setExecutable(true, false)
        } catch (e: Exception) {
            android.util.Log.e("EduApplication", "Failed to pre-create WebView cache dir: ${e.message}")
        }

        // 2. Initialize WebView multi-process data directory suffix to prevent lock and database conflict errors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val processName = Application.getProcessName()
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            } catch (e: Exception) {
                android.util.Log.e("EduApplication", "Error setting WebView data directory suffix: ${e.message}")
            }
        }
    }
}
