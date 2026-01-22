package com.app.gemvox.data.network

import android.util.Base64
import android.util.Log
import com.app.gemvox.BuildConfig
import com.app.gemvox.data.network.dto.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSocketClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) : WebSocketListener() {
    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow(false)
    val connectionState = _connectionState.asStateFlow()

    private val _audioOutput = MutableSharedFlow<ByteArray>()
    val audioOutput = _audioOutput.asSharedFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun connect() {
        if (_connectionState.value) return
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=${BuildConfig.GEMINI_API_KEY}"
        Log.d("GeminiRaw", "Connecting to URL: $url")
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = false
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("GemVoxWebsocket", "WebSocket Connected")
        _connectionState.value = true
        sendSetupMessage()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("GeminiRaw", "RECEIVED: $text")
        try {
            val response = json.decodeFromString<BidiReceivePayload>(text)
            response.serverContent?.modelTurn?.parts?.forEach { part ->
                part.inlineData?.let { audio ->
                    if (audio.mimeType.startsWith("audio")) {
                        val pcmBytes = Base64.decode(audio.data, Base64.DEFAULT)
                        _audioOutput.tryEmit(pcmBytes)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GemVoxWebsocket", "Error parsing: ${e.message}")
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("GeminiRaw", "Closing: $code / $reason")

        if (code == 1011 || reason.contains("quota", true)) {
            _errorMessage.value = "Daily Limit Exceeded. Please wait or use a new Key."
        } else {
            _errorMessage.value = "Disconnected: $reason"
        }

        _connectionState.value = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("GemVoxWebsocket", "Failure: ${t.message}")
        _errorMessage.value = "Connection Failed: ${t.localizedMessage}"
        _connectionState.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
    private fun sendSetupMessage() {
        val msg = BidiSendPayload(
            setup = GeminiSetup(
                model = "models/gemini-2.0-flash-exp",
                generationConfig = GenerationConfig(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = SpeechConfig(
                        voiceConfig = VoiceConfig(
                            prebuiltVoiceConfig = PrebuiltVoiceConfig("Puck")
                        )
                    )
                )
            )
        )
        val jsonString = json.encodeToString(BidiSendPayload.serializer(), msg)
        Log.d("GeminiRaw", "SENDING SETUP: $jsonString")
        webSocket?.send(jsonString)
    }

    fun sendAudio(pcmData: ByteArray) {
        val base64 = Base64.encodeToString(pcmData, Base64.NO_WRAP)
        val msg = BidiSendPayload(
            realtimeInput = RealtimeInput(
                mediaChunks = listOf(
                    MediaChunk(
                        mimeType = "audio/pcm",
                        data = base64
                    )
                )
            )
        )
        val jsonString = json.encodeToString(BidiSendPayload.serializer(), msg)
        webSocket?.send(jsonString)
    }
}