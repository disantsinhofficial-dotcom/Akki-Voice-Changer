package com.akki.voicechanger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

private data class VoicePreset(val name: String, val description: String, val ratio: Float)

@Composable
private fun AkkiVoiceChangerApp() {
    val context = LocalContext.current
    val presets = remember {
        listOf(
            VoicePreset("Aanya · Natural", "Higher, softer tone", 1.28f),
            VoicePreset("Mira · Soft", "Gentle bright pitch", 1.20f),
            VoicePreset("Kiara · Bright", "Brighter, higher pitch", 1.38f),
            VoicePreset("Tara · Deep", "Lower pitch effect", 0.88f)
        )
    }
    var selected by remember { mutableStateOf(presets[0]) }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var running by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready · choose a voice effect and start") }
    val engine = remember { LiveVoiceEngine(context) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        status = if (granted) "Microphone permission granted" else "Microphone permission is required"
    }

    DisposableEffect(Unit) {
        onDispose { engine.stop() }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF090D18)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth().background(
                            Brush.linearGradient(listOf(Color(0xFF56349A), Color(0xFF087E91))),
                            RoundedCornerShape(28.dp)
                        ).padding(24.dp)
                    ) {
                        Text("AKKI", color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
                        Text("Voice Changer", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                        Text("Offline live pitch effects", color = Color.White.copy(alpha = .85f))
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2D))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Choose voice effect", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("These are real-time pitch presets, not AI voice-clone models.", color = Color(0xFFB8C3D8))
                        }
                    }
                }
                items(presets.size) { index ->
                    val preset = presets[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { if (!running) selected = preset },
                        colors = CardDefaults.cardColors(containerColor = if (selected == preset) Color(0xFF263957) else Color(0xFF151D2D)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected == preset, onClick = { if (!running) selected = preset })
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(preset.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(preset.description, color = Color(0xFFB8C3D8), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2D))) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Live microphone", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                if (running) "Mic is being processed and monitored through the phone speaker. Use headphones to reduce feedback."
                                else "Start to hear your microphone with the selected pitch effect. Headphones are recommended.",
                                color = Color(0xFFB8C3D8)
                            )
                            if (!hasPermission) {
                                Button(onClick = { permission.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Allow microphone")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (running) {
                                            engine.stop()
                                            running = false
                                            status = "Stopped"
                                        } else {
                                            try {
                                                engine.start(selected.ratio)
                                                running = true
                                                status = "Live effect active · ${selected.name}"
                                            } catch (e: Exception) {
                                                running = false
                                                status = "Could not start audio: ${e.message ?: "audio device unavailable"}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (running) "Stop voice changer" else "Start live voice changer") }
                            }
                            Text(status, color = if (running) Color(0xFF8ED8C5) else Color(0xFFB8C3D8))
                        }
                    }
                }
                item {
                    Text(
                        "Note: This build provides offline live pitch shifting. It does not yet create a natural AI female voice, save converted recordings, or alter cellular SIM-call microphone audio. Ordinary Android apps cannot generally replace SIM-call uplink audio on non-rooted phones.",
                        color = Color(0xFF8491A8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }
    }
}
