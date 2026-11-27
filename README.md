# Akki Voice Changer

Android offline voice-effects app.

## Current build

The current implementation adds an experimental live microphone pitch monitor with four selectable pitch presets. It processes PCM locally with Android AudioRecord/AudioTrack and does not upload microphone audio.

## Important limitations

- Pitch shifting changes both pitch and playback speed. It is not an AI female-voice conversion model and does not reproduce a natural female vocal timbre.
- This initial live monitor is intended for foreground use and headphones are recommended to prevent acoustic feedback.
- Converted recording/export, downloaded local AI voice models, neural real-time conversion, and in-app VoIP calling are not implemented yet.
- A normal non-rooted Android app generally cannot replace the microphone uplink in ordinary cellular SIM calls. Do not expect SIM-call voice changing.

## Build

Open in Android Studio or run `gradle assembleDebug` with JDK 17 and Android SDK 35.
