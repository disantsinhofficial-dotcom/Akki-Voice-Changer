# Akki Voice Changer

An Android-first, offline voice conversion project.

## Project status

This repository is being initialized. The first milestone is a buildable Android application shell and audio-engine foundation. Real-time voice conversion and calling support must be validated on target hardware before being described as supported.

## Important Android limitation

Standard non-rooted Android apps generally cannot replace the microphone uplink audio in ordinary cellular SIM calls. Internet calling can only be supported when the call is handled inside this app (or through a compatible calling integration).

## Planned milestones

1. Buildable Android app and permission/audio-device diagnostics.
2. Local voice profile/model management and preview pipeline.
3. Low-latency offline voice conversion, measured on real devices.
4. In-app internet calling integration, separately validated.
5. Device compatibility testing and release builds.
