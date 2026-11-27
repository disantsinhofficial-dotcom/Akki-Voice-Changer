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
            VoicePreset("Aanya · Higher", "Higher pitch; voice also becomes faster", 1.28f),
            VoicePreset("Mira · Gentle", "Slightly raised pitch and speed", 1.20f),
            VoicePreset("Kiara · Bright", "Strong raised pitch and speed", 1.38f),
            VoicePreset("Tara · Lower", "Lower pitch and slower playback", 0.88f)
        )
    }
    var selected by remember { mutableStateOf(presets[0]) }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var running by remember { mutableStateOf(false) }
    var showSimInfo by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready · headphones recommended") }
    val engine = remember { LiveVoiceEngine(context) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        status = if (granted) "Microphone permission granted" else "Microphone permission denied"
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
                        Text("Offline microphone pitch effects", color = Color.White.copy(alpha = .85f))
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF392A20))) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("SIM call limitation", color = Color(0xFFFFD6A0), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("This app cannot change your voice heard by the other person on a normal SIM call on a standard non-rooted Android phone.", color = Color.White)
                            TextButton(onClick = { showSimInfo = true }) { Text("Why doesn't SIM call work?") }
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2D))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Choose voice effect", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("These presets shift pitch by changing playback speed. They are not AI female voice models.", color = Color(0xFFB8C3D8))
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
                            Text("Live microphone test", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                if (running) "Effect active. Your processed voice is played locally on this phone only; it is not sent into a call."
                                else "Test the effect locally using headphones. Loudspeaker feedback can cause echo.",
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
                                                status = "Local pitch monitor active · ${selected.name}"
                                            } catch (e: Exception) {
                                                running = false
                                                status = "Audio start failed: ${e.message ?: "device unavailable"}"
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (running) "Stop local test" else "Start local test") }
                            }
                            Text(status, color = if (running) Color(0xFF8ED8C5) else Color(0xFFB8C3D8))
                        }
                    }
                }
                item {
                    Text(
                        "Not supported: changing audio in normal cellular SIM calls or other apps' calls. Also not yet included: neural AI voice conversion, converted recording export, background operation and in-app internet calling.",
                        color = Color(0xFF8491A8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
            if (showSimInfo) {
                AlertDialog(
                    onDismissRequest = { showSimInfo = false },
                    title = { Text("Why SIM call voice changing is unavailable") },
                    text = { Text("Android protects cellular call audio. A regular app without privileged system access cannot capture and replace the outgoing SIM-call microphone stream. Microphone permission alone does not grant this ability. This app's effect can only be tested locally.") },
                    confirmButton = { TextButton(onClick = { showSimInfo = false }) { Text("Got it") } }
                )
            }
        }
    }
}
