package com.akki.voicechanger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AkkiVoiceChangerApp() }
    }
}

@Composable
private fun AkkiVoiceChangerApp() {
    val context = LocalContext.current
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var selectedVoice by remember { mutableStateOf("Aanya · Natural") }
    var status by remember { mutableStateOf("Offline mode · Voice engine setup in progress") }
    val voices = listOf(
        "Aanya · Natural",
        "Mira · Soft",
        "Kiara · Bright",
        "Tara · Deep"
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        status = if (granted) "Microphone permission granted" else "Microphone permission denied"
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF090D18)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4D2D91), Color(0xFF126C83))),
                                RoundedCornerShape(28.dp)
                            )
                            .padding(22.dp)
                    ) {
                        Text("AKKI", color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
                        Text("Voice Changer", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Offline voice studio", color = Color.White.copy(alpha = .85f))
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2D))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Microphone access", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                if (micGranted) "Permission granted" else "Allow microphone access to prepare live audio input.",
                                color = Color(0xFFB8C3D8)
                            )
                            Button(onClick = {
                                if (!micGranted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                else status = "Microphone permission is already enabled"
                            }) {
                                Text(if (micGranted) "Permission enabled" else "Allow microphone")
                            }
                        }
                    }
                }
                item {
                    Text("Choose a female voice", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                items(voices) { voice ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedVoice == voice) Color(0xFF263957) else Color(0xFF151D2D)
                        ),
                        onClick = { selectedVoice = voice }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedVoice == voice, onClick = { selectedVoice = voice })
                            Text(voice, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2D))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Voice preview", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Selected: $selectedVoice",
                                color = Color(0xFFB8C3D8)
                            )
                            OutlinedButton(onClick = { status = "Preview will be enabled after the offline voice model is integrated." }) {
                                Text("Preview · engine not ready")
                            }
                        }
                    }
                }
                item {
                    Text(status, color = Color(0xFF8ED8C5), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "This starter build does not yet convert speech. Offline model, real-time processing and calling support are planned for later milestones.",
                        color = Color(0xFF8491A8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                }
            }
        }
    }
}
