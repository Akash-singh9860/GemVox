package com.app.gemvox.domain.repository

import com.app.gemvox.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    suspend fun connect()
    suspend fun disconnect()
    fun getConnectionState(): Flow<ConnectionState>
    fun getIncomingAudioStream(): Flow<ByteArray>
    suspend fun sendAudio(pcmData: ByteArray)
}