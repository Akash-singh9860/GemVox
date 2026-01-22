# 💎 GemVox

**Real-time, bidirectional voice assistant powered by Gemini 2.0 Flash.**

<img width="250" height="750" alt="image" src="https://github.com/user-attachments/assets/d25e6548-e02b-4536-8f12-b5ac2ee222f4" />


## 🚀 Overview

**GemVox** is a cutting-edge Android application that demonstrates **real-time, low-latency voice conversations** using Google's **Gemini 2.0 Flash Experimental** model.

Unlike traditional voice assistants that rely on slow Request/Response (HTTP) cycles, GemVox utilizes a persistent **WebSocket** connection to stream raw PCM audio bi-directionally. This architecture allows for near-instant responses, interruptibility, and a natural conversational flow.

Designed with a "headless" philosophy, GemVox runs as a Foreground Service, allowing users to interact with the AI even when the phone is locked by using **headphone media buttons**.

## ✨ Key Features

* **⚡ Real-Time Bidi Streaming:** Leverages the `v1alpha` WebSocket API for simultaneous audio input and output.
* **🔒 Headless "Walkie-Talkie" Mode:** Runs as a Foreground Service. Control the microphone using **Play/Pause** buttons on your headset or lock screen.
* **🎧 Low Latency Audio:** Direct PCM streaming (16kHz Input / 24kHz Output) using `AudioRecord` and `ExoPlayer` (Media3).
* **🛠️ Clean Architecture:** Strict separation of concerns (Presentation → Domain → Data).
* **🎨 Modern UI:** Built entirely with **Jetpack Compose** and Material 3.
* **🧭 Navigation 3:** Implements the modern, type-safe **Navigation 3** library (`NavDisplay`).

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM + Clean Architecture
* **Dependency Injection:** Hilt (Dagger)
* **Network:** OkHttp (WebSockets)
* **Audio:** Android Media3 (ExoPlayer), AudioRecord
* **Concurrency:** Kotlin Coroutines & Flows
* **Serialization:** Kotlinx Serialization

## 🏗️ Architecture

The app follows a strict uni-directional data flow:

1.  **Service Layer (`GemVoxService`):** Orchestrates the `MediaSession`, handles hardware button events, and manages the Foreground Notification.
2.  **Domain Layer (`VoiceRepository`):** Defines the abstract interface for sending/receiving voice data.
3.  **Data Layer (`GeminiSocketClient`):** Manages the raw WebSocket connection, handles the Gemini Bidi JSON protocol, and performs Base64 encoding/decoding of audio chunks.

## 🚀 Getting Started

### Prerequisites

1.  Android Studio (Koala/Ladybug or newer recommended).
2.  A Google Cloud Project with the **Gemini API** enabled.
3.  An active API Key from [Google AI Studio](https://aistudio.google.com/).

### Installation

1.  **Clone the repository:**
2.  **Configure API Key:**
3.  **Build & Run:**
    * Sync Project with Gradle Files.
    * Deploy to a **Physical Device** (Microphone routing on emulators is often unreliable for this specific use case).

## 🎮 How to Use

1.  **Launch:** Open the app and grant the required **Microphone** and **Notification** permissions.
2.  **Start:** Tap the large **Mic Button** on the Live screen. A notification ("Tap Play to Speak") will appear.
3.  **Background Mode:** You can now lock your phone screen.
4.  **Talk:**
    * Press the **Play/Pause** button on your wired or Bluetooth headphones.
    * Speak your query (e.g., *"Hello Gemini, tell me a fun fact"*).
    * The AI response will stream immediately into your ears.

## ⚠️ Limitations & Quotas

* **Experimental Model:** This app uses `gemini`. While free, it has strict **Requests Per Minute (RPM)** and **Requests Per Day (RPD)** limits.
* **Quota Error (1011):** If the socket disconnects with code `1011`, you have likely exceeded your daily quota for the experimental model.
* **Audio Format:** Configured for 16-bit PCM Mono audio to match Gemini's strict requirements.

## 🤝 Contributing
Contributions are welcome! Please open an issue to discuss major changes before submitting a pull request.
