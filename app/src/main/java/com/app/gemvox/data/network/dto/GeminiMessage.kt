package com.app.gemvox.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BidiSendPayload(
    val setup: GeminiSetup? = null,
    @SerialName("realtime_input")
    val realtimeInput: RealtimeInput? = null,
    @SerialName("client_content")
    val clientContent: ClientContent? = null
)

@Serializable
data class GeminiSetup(
    val model: String = "models/gemini-2.0-flash-exp",
    @SerialName("generation_config")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GenerationConfig(
    @SerialName("response_modalities")
    val responseModalities: List<String> = listOf("AUDIO"),
    @SerialName("speech_config")
    val speechConfig: SpeechConfig? = null
)

@Serializable
data class SpeechConfig(
    @SerialName("voice_config")
    val voiceConfig: VoiceConfig
)

@Serializable
data class VoiceConfig(
    @SerialName("prebuilt_voice_config")
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@Serializable
data class PrebuiltVoiceConfig(
    @SerialName("voice_name")
    val voiceName: String
)

@Serializable
data class RealtimeInput(
    @SerialName("media_chunks")
    val mediaChunks: List<MediaChunk>
)

@Serializable
data class MediaChunk(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String
)

@Serializable
data class ClientContent(
    val turns: List<Turn>,
    @SerialName("turn_complete")
    val turnComplete: Boolean = false
)

@Serializable
data class Turn(
    val role: String = "user",
    val parts: List<Part>
)


@Serializable
data class BidiReceivePayload(
    @SerialName("server_content")
    val serverContent: ServerContent? = null,
    @SerialName("tool_call")
    val toolCall: ToolCall? = null
)

@Serializable
data class ServerContent(
    @SerialName("model_turn")
    val modelTurn: ModelTurn? = null,
    @SerialName("turn_complete")
    val turnComplete: Boolean = false
)

@Serializable
data class ModelTurn(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String
)

@Serializable
data class ToolCall(
    @SerialName("function_calls")
    val functionCalls: List<String>? = null
)