package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.AppDatabase
import com.example.data.EduRepository
import com.example.ui.EduGuideApp
import com.example.ui.EduViewModel
import com.example.ui.EduViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database & DAO
    val database = AppDatabase.getDatabase(this)
    val repository = EduRepository(this, database.eduDao())

    // Initialize Google Mobile Ads SDK
    try {
        com.google.android.gms.ads.MobileAds.initialize(this) { status ->
            android.util.Log.d("AdMob", "AdMob MobileAds SDK Initialized successfully!")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdMob", "AdMob SDK initialisation error: ${e.message}")
    }

    // Initialize local custom Viewmodel with local Factory mapping
    val viewModelByFactory: EduViewModel by viewModels {
      EduViewModelFactory(repository)
    }

    setContent {
      val isDarkTheme by viewModelByFactory.isDarkTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          EduGuideApp(viewModel = viewModelByFactory)
        }
      }
    }
  }
}

