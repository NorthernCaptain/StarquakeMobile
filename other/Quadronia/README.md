# Quadronia

A cross-platform puzzle game built with libGDX.

## About the Game

**Easy to start, hard to master match-color game.**

Find rectangle areas marked with the same color quads in the corners. Clear areas by clicking diagonal corners, earn score points, collect bonuses and perks, survive as long as you can.

### Features

- Intuitive touch/click gameplay
- Multiple game modes
- Achievements and leaderboards (Google Play Games / Game Center)
- Bonuses and power-ups
- Cross-platform support

## Supported Platforms

| Platform | Min Version | Status |
|----------|-------------|--------|
| Android | API 24 (Android 7.0) | Production |
| iOS | iOS 12+ | Production |
| Desktop | Windows/macOS/Linux | Development |

## Tech Stack

### Core Framework
- **libGDX** 1.14.0 - Cross-platform game development framework
- **RoboVM** 2.3.24 - iOS backend (Java to native iOS compilation)
- **LWJGL3** - Desktop backend

### Build Tools
- **Gradle** 8.9
- **Android Gradle Plugin** 8.7.3
- **Java** 8 (source compatibility)

### Platform-Specific
- **Android**
  - Target SDK: 35 (Android 15)
  - Compile SDK: 35
  - Google Play Games Services v2: 21.0.0
  - Architectures: arm64-v8a, armeabi-v7a, x86_64, x86

- **iOS**
  - RoboVM for AOT compilation
  - GameKit integration for Game Center
  - Architecture: arm64

### Additional Libraries
- **gdx-freetype** - TrueType font rendering
- **org.json** - JSON parsing (desktop)

## Project Structure

```
Quadronia/
├── core/           # Shared game logic and assets handling
├── android/        # Android-specific code and resources
│   ├── assets/     # Game assets (shared with all platforms)
│   ├── res/        # Android resources
│   └── src/        # Android launcher and platform code
├── ios/            # iOS-specific code and configuration
│   ├── data/       # iOS-specific resources
│   └── src/        # iOS launcher and platform code
├── desktop/        # Desktop launcher
└── build.gradle    # Root build configuration
```

## Prerequisites

### All Platforms
- JDK 8 or higher
- Gradle 8.9+ (wrapper included)

### Android
- Android SDK with API 35
- Android NDK (for native libraries)

### iOS
- macOS with Xcode
- Valid Apple Developer account (for device deployment)

### Desktop
- No additional requirements

## Building

### Android

```bash
# Debug APK
./gradlew :android:assembleDebug

# Release APK
./gradlew :android:assembleRelease

# Install and run on connected device
./gradlew :android:installDebug :android:run
```

Output: `android/build/outputs/apk/`

### iOS

```bash
# Build
./gradlew :ios:build

# Run on iPhone Simulator
./gradlew :ios:launchIPhoneSimulator

# Run on iPad Simulator
./gradlew :ios:launchIPadSimulator

# Run on connected device
./gradlew :ios:launchIOSDevice

# Create IPA for App Store
./gradlew :ios:createIPA
```

### Desktop

```bash
# Run directly
./gradlew :desktop:run

# Create distributable JAR
./gradlew :desktop:dist
```

Output: `desktop/build/libs/`

## Configuration

### Android Signing

The release signing configuration is in `android/build.gradle`. Update the keystore path and credentials:

```groovy
signingConfigs {
    releaseConfig {
        storeFile file("path/to/keystore")
        storePassword "your-password"
        keyAlias "your-alias"
        keyPassword "your-key-password"
    }
}
```

### iOS Signing

Update `ios/build.gradle` with your provisioning profile and signing identity:

```groovy
robovm {
    iosSignIdentity = "iPhone Distribution: Your Name (TEAM_ID)"
    iosProvisioningProfile = "YourProvisioningProfile"
    iosSkipSigning = false
    archs = "arm64"
}
```

## Development

### Running from IDE

1. Import as Gradle project in IntelliJ IDEA or Android Studio
2. For desktop testing, run the `desktop:run` Gradle task
3. For Android, use the standard Android run configuration

### Assets

All game assets are stored in `android/assets/` and shared across platforms. This includes:
- Textures and sprite atlases
- Audio files
- Fonts
- Configuration files

### Adding New Dependencies

Add dependencies to the appropriate section in the root `build.gradle`:
- `project(":core")` - Shared dependencies
- `project(":android")` - Android-specific
- `project(":ios")` - iOS-specific
- `project(":desktop")` - Desktop-specific

## Version History

See git log for detailed changes.

Current version: **1.6.2**

## License

Proprietary - All rights reserved.

## Author

Northern Captain
