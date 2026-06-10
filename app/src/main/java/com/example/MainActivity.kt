package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StartupViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Firebase Services with graceful local sandbox fallbacks
    com.example.data.remote.FirebaseService.initialize(applicationContext)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppScreen(
          viewModel = viewModel<StartupViewModel>(),
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
