# RelationshipRadar 📱

A Pixel-first personal relationship assistant that tracks real interactions and helps maintain relationships without becoming another task list.

## Core idea

RelationshipRadar answers:

> "When was the last time I actually reached out to this person?"

## Design principles

- Track all saved contacts by default.
- Keep tracking separate from reminders.
- Use real interaction history during setup.
- Avoid notification overload.
- Make ADHD-friendly relationship maintenance easier.

## Platform

Initial target:

- Google Pixel / Android
- Shizuku integrated for enhanced device access

See `/docs` for product decisions and rules.

## Building & testing locally

Requirements on this Mac: Android SDK at `~/Library/Android/sdk`, JDK 17+ (Android Studio's bundled JBR works).

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

### Emulator (preferred — never install test builds on the real Pixel without asking)

A Pixel AVD named **`Pixel_Radar`** (Android 37.1, Google APIs, arm64) lives in `~/.android/avd`.

```bash
# boot (headless is fine for agents; drop -no-window to watch)
~/Library/Android/sdk/emulator/emulator -avd Pixel_Radar -no-window -no-audio -no-boot-anim &
~/Library/Android/sdk/platform-tools/adb wait-for-device

# install + launch
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
~/Library/Android/sdk/platform-tools/adb shell am start -n com.relationshipradar.app/.MainActivity

# read the screen (don't guess taps — dump, then tap by bounds)
~/Library/Android/sdk/platform-tools/adb shell uiautomator dump /sdcard/ui.xml
~/Library/Android/sdk/platform-tools/adb pull /sdcard/ui.xml /tmp/ui.xml
```

Only one agent should drive the emulator at a time. If the AVD is missing, recreate it:

```bash
~/Library/Android/sdk/cmdline-tools/latest/bin/avdmanager create avd -n Pixel_Radar -k "system-images;android-37.1;google_apis_ps16k;arm64-v8a" -d pixel_9
```

The real Pixel shows up in `adb devices` when plugged in; use `adb -s <serial>` to pick a target explicitly.
