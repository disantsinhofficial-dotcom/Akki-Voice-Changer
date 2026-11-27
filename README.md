# Akki Voice Changer

Android offline local pitch-effects test app.

## Current implementation

- Foreground microphone capture with Android AudioRecord.
- Local pitch/speed effect playback through AudioTrack.
- Four selectable pitch presets.
- Microphone permission handling and audio initialization errors.
- Clear in-app explanation of SIM-call restrictions.

## Known limitations (not bugs that an ordinary app can fix)

- The effect is a playback-rate pitch shift, so pitch and speed change together. It is not neural AI voice conversion and does not sound like a natural cloned female voice.
- It only monitors processed audio locally on the phone. It does not inject processed audio into SIM calls or other apps' calls.
- Standard non-rooted Android apps do not have permission to replace the microphone uplink for cellular calls.
- Converted recording export, AI voice models, background processing and in-app VoIP calling are not implemented.

## Build

JDK 17, Android SDK 35, Gradle: `assembleDebug`.
