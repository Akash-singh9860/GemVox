package com.app.gemvox.data.repository

import com.app.gemvox.data.network.GeminiSocketClient
import com.app.gemvox.domain.model.ConnectionState
import com.app.gemvox.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VoiceRepositoryImpl @Inject constructor(
    private val socketClient: GeminiSocketClient
) : VoiceRepository {

    override suspend fun connect() {
        socketClient.connect()
    }

    override suspend fun disconnect() {
        socketClient.disconnect()
    }

    override fun getConnectionState(): Flow<ConnectionState> {
        return socketClient.connectionState.map { isConnected ->
            if (isConnected) ConnectionState.Connected else ConnectionState.Disconnected
        }
    }

    override fun getIncomingAudioStream(): Flow<ByteArray> {
        return socketClient.audioOutput
    }

    override suspend fun sendAudio(pcmData: ByteArray) {
        socketClient.sendAudio(pcmData)
    }
}