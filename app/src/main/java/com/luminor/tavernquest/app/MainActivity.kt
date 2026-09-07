package com.luminor.tavernquest.app
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luminor.tavernquest.core.designsystem.theme.TavernQuestTheme
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint class MainActivity : ComponentActivity() {
 private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS); setContent { TavernQuestTheme { TavernQuestApp() } } }
}
