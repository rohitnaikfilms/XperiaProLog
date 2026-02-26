# Xperia ProLog

A production-grade, professional cinema camera application specifically optimized for the Sony Xperia 1 V. Built with Material Design 3 and the Android Camera2 API to provide full manual control, 10-bit HEVC recording, and a raw cinema experience.

## Features

- **Full Manual Control**: Complete control over ISO, Shutter Speed (standard cinema angles), Focus (manual focus pull), and White Balance.
- **10-Bit HEVC Recording**: Native 10-bit hardware-accelerated encoding using `MediaCodec`.
- **True Mathematical Color Profiles**: Record in standard Rec.709, hardware HLG (Rec.2020), or specialized Log curves like Kodak Cineon and Sony S-Log3. These are processed mathematically via custom 10-bit EGL/GLES3 Shaders inside `GL10BitRenderer.kt` for zero loss of precision.
- **Physical Lens Switching**: Direct access to the Xperia 1 V's triple camera system (Ultra-wide, Wide, Telephoto) utilizing Camera2 logical streams.
- **Professional Monitoring**:
  - Cinema scope (2.39:1) frame guides
  - Rule of thirds grid
  - Center crosshairs
- **Uncompressed Audio**: Uses `AudioRecord` for capturing raw 48KHz PCM audio.
- **Dark UI**: Material You dark theme optimized for a distraction-free filmmaking experience, inspired by Sony Cinema Pro and RED Komodo interfaces.

## Architecture

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (MD3)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow.
- **Camera API**: `Camera2` directly (instead of CameraX) to guarantee access to manufacturer-specific extensions and unabstracted sensor data.

### Core Modules

- `CameraDeviceManager`: Handles querying hardware characteristics and enumerating 10-bit logical/physical lenses.
- `CameraViewModel`: Unified `StateFlow` managing the active `ExposureState` (ISO, Shutter, Focus, Lens).
- `VideoEncoder` / `AudioCaptureManager`: Manual media pipeline recording straight to H.265 / PCM streams.

## Requirements

- Sony Xperia 1 V (or a device supporting Camera2 API Level 3 physical streams and 10-bit capture).
- Android 13+ (API Level 33+).

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Iguana or newer recommended).
3. Connect your Android device. (Note: An emulator cannot emulate the specialized 10-bit Camera2 characteristics of the Xperia).
4. Build and deploy. Ensure you grant Camera, Microphone, and Storage permissions upon first launch.

## License

Copyright (c) 2026. All Rights Reserved.
