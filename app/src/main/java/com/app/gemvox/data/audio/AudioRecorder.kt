package com.app.gemvox.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<ByteArray> = flow {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecorder", "Audio Record initialization failed")
                return@flow
            }

            audioRecord?.startRecording()
            Log.d("AudioRecorder", "Recording started")

            val buffer = ByteArray(1024)

            while (currentCoroutineContext().isActive) {
                val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (readResult > 0) {
                    emit(buffer.copyOfRange(0, readResult))
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Recording error: ${e.message}")
        } finally {
            stopRecording()
        }
    }

    private fun stopRecording() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d("AudioRecorder", "Recording stopped")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping: ${e.message}")
        }
    }
}