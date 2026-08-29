# LandRadar Android

Android client for LandRadar — a lightweight property discovery companion.

## Mobile scope

- Basic property search and filters
- Compact map with interesting/saved property pins
- Short property details
- Save/star action
- Essential tracked-property notifications
- Deep analysis remains on the main LandRadar website

## Current state

Initial Kotlin + Jetpack Compose scaffold is present. Authentication boundaries and encrypted token storage are defined, but no production backend/provider is connected. The current OTP button demonstrates navigation only and must not be treated as real authentication.

See [Authentication architecture](docs/AUTHENTICATION.md).

## Local setup

Requirements: Android Studio, JDK 17, Android SDK 35, and Gradle compatible with Android Gradle Plugin 8.7.3.

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Gradle sync dependencies.
4. Create/run the `app` configuration on an Android 8.0+ emulator or device.

Do not commit `local.properties`, keystores, environment files, credentials, or tokens.
