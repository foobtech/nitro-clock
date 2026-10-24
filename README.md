# Nitro Clock

Alarms, world clock, timer and stopwatch for NitroDroid. Jetpack Compose, Material 3 Expressive theme
(Material You colors on Android 12+). No Google services, no trackers, no network permission.

Package: `dev.nitrodroid.clock` · min Android 9 (API 28) · target API 36

## Build and run (Android Studio)

1. Install the latest Android Studio (free): https://developer.android.com/studio
2. Unzip this project, then in Android Studio choose File > Open and select the `NitroClock` folder.
3. Let Gradle sync finish (first sync downloads a few hundred MB). If it offers to update the Android
   Gradle Plugin or Kotlin, accept; if it reports a version error, send me the message.
4. Pick a device: Device Manager > Create Virtual Device (any Pixel profile with a recent system
   image), or plug in a phone with USB debugging on.
5. Press the green Run button.
6. On first launch, allow notifications. Without that permission the alarm still rings but the
   on-screen controls may not appear.

### Build the APK without Android Studio (free, on GitHub)

Push this project to a GitHub repo. `.github/workflows/build.yml` builds a debug APK on GitHub's
servers; download it from the Actions tab (run > Artifacts > NitroClock-debug-apk).

### Command line

    gradle wrapper        # once, generates gradlew and gradle-wrapper.jar (needs Gradle installed)
    ./gradlew assembleDebug
    # APK: app/build/outputs/apk/debug/app-debug.apk

## Try it

- Alarm: New alarm, set a time one or two minutes ahead, Save, lock the screen and wait.
- Timer: tap 1 min and let it run out.
- Clock: Add city, search "Tokyo".
- Emulators sometimes ring silently; check the emulator's volume.

## Known limits in v0.1

- Not yet built or tested by the author; expect to fix a compile error or two on first sync.
- Alarm sound is the system default alarm tone (no sound picker yet).
- Stopwatch resets if Android kills the app.
- No bedtime reminders, gradual volume or per-alarm snooze length yet.
- Expressive theme only; expressive components (button groups, wavy progress) come later.

## F-Droid checklist

- [ ] Pick a license (GPL-3.0-or-later or Apache-2.0 are common) and add a LICENSE file
- [ ] Commit `gradlew` and `gradle/wrapper/gradle-wrapper.jar`
- [ ] Push to a public repo and tag a release (for example `v0.1.0`)
- [ ] Add `fastlane/metadata/android/en-US/` (title, short and full description, screenshots)
- [ ] Open a merge request to https://gitlab.com/fdroid/fdroiddata with a metadata YAML for the app
