package com.akki.voicechanger

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioManager
import kotlin.concurrent.thread
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Low-latency experimental live pitch monitor.
 * Playback-rate pitch shifting changes both pitch and speed; it is not a neural voice-conversion model.
 */
class LiveVoiceEngine(context: Context) {
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var worker: Thread? = null
    private val active = AtomicBoolean(false)
    private var oldMode = AudioManager.MODE_NORMAL
    private var oldSpeaker = false

    @Suppress("DEPRECATION")
    @Synchronized
    fun start(pitchRatio: Float) {
        if (active.get()) return
        require(pitchRatio in 0.75f..1.55f) { "Unsupported pitch preset" }
        val sampleRate = 44100
        val minIn = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val minOut = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        check(minIn > 0 && minOut > 0) { "Audio format is not supported" }
        val bufferSize = maxOf(minIn, minOut, 4096) * 2
        val input = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        check(input.state == AudioRecord.STATE_INITIALIZED) { input.release(); "Microphone initialization failed" }
        val output = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder().setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        check(output.state == AudioTrack.STATE_INITIALIZED) {
            input.release(); output.release(); "Audio output initialization failed"
        }
        oldMode = audioManager.mode
        oldSpeaker = audioManager.isSpeakerphoneOn
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        try {
            output.setPlaybackRate((sampleRate * pitchRatio).toInt())
            input.startRecording()
            output.play()
        } catch (e: Exception) {
            input.release()
            output.release()
            audioManager.mode = oldMode
            audioManager.isSpeakerphoneOn = oldSpeaker
            throw e
        }
        recorder = input
        player = output
        active.set(true)
        worker = thread(name = "AkkiLivePitch", priority = Thread.MAX_PRIORITY) {
            val pcm = ShortArray(2048)
            while (active.get()) {
                val count = input.read(pcm, 0, pcm.size, AudioRecord.READ_BLOCKING)
                if (count > 0 && active.get()) {
                    val written = output.write(pcm, 0, count, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) break
                } else if (count < 0) {
                    break
                }
            }
            active.set(false)
        }
    }

    @Suppress("DEPRECATION")
    @Synchronized
    fun stop() {
        active.set(false)
        try { recorder?.stop() } catch (_: Exception) {}
        try { player?.pause(); player?.flush(); player?.stop() } catch (_: Exception) {}
        try { worker?.join(300) } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
        recorder?.release()
        player?.release()
        recorder = null
        player = null
        worker = null
        audioManager.mode = oldMode
        audioManager.isSpeakerphoneOn = oldSpeaker
    }
}
