package com.akki.voicechanger

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Foreground-only local pitch monitor. Playback-rate shifting also changes speed.
 * This class does not route audio into cellular or third-party app calls.
 */
class LiveVoiceEngine(context: Context) {
    private val appContext = context.applicationContext
    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var worker: Thread? = null
    private val active = AtomicBoolean(false)

    @Synchronized
    fun start(pitchRatio: Float) {
        if (active.get()) return
        require(pitchRatio in 0.75f..1.55f) { "Unsupported pitch preset" }

        val sampleRate = 44100
        val minIn = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val minOut = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        check(minIn > 0) { "Microphone does not support the requested audio format ($minIn)" }
        check(minOut > 0) { "Speaker does not support the requested audio format ($minOut)" }
        val bufferSize = maxOf(minIn, minOut, 8192)

        val input = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (input.state != AudioRecord.STATE_INITIALIZED) {
            input.release()
            error("Microphone initialization failed")
        }

        val output = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        if (output.state != AudioTrack.STATE_INITIALIZED) {
            input.release()
            output.release()
            error("Audio output initialization failed")
        }

        try {
            output.setPlaybackRate((sampleRate * pitchRatio).toInt())
            input.startRecording()
            check(input.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                "Microphone did not enter recording state"
            }
            output.play()
        } catch (t: Throwable) {
            try { input.stop() } catch (_: Exception) {}
            input.release()
            output.release()
            throw t
        }

        recorder = input
        player = output
        active.set(true)
        worker = thread(name = "AkkiLivePitch", priority = Thread.MAX_PRIORITY) {
            val pcm = ShortArray(2048)
            try {
                while (active.get()) {
                    val count = input.read(pcm, 0, pcm.size, AudioRecord.READ_BLOCKING)
                    if (count > 0 && active.get()) {
                        val written = output.write(pcm, 0, count, AudioTrack.WRITE_BLOCKING)
                        if (written < 0) break
                    } else if (count < 0) {
                        break
                    }
                }
            } catch (_: Exception) {
                // Audio device can be disconnected or reclaimed by Android.
            } finally {
                active.set(false)
            }
        }
    }

    @Synchronized
    fun stop() {
        active.set(false)
        val input = recorder
        val output = player
        try { input?.stop() } catch (_: Exception) {}
        try { output?.pause() } catch (_: Exception) {}
        try { output?.flush() } catch (_: Exception) {}
        try { worker?.join(1000) } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        try { input?.release() } catch (_: Exception) {}
        try { output?.release() } catch (_: Exception) {}
        recorder = null
        player = null
        worker = null
    }
}
