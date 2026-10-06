# KeepJ GitHub APK build

KeepJ is a native Android Jetpack Compose application.

## Supported devices

- Android 8.0 (API 26) and newer
- `minSdk 26`
- `targetSdk 35`
- `compileSdk 35`
- Java 17

## Build an APK on GitHub

1. Push this repository to GitHub.
2. Open **Actions**.
3. Select **Build Android APK**.
4. Choose **Run workflow**.
5. Wait for the build to finish.
6. Open the completed run.
7. Download the **keepj-debug-apk** artifact.
8. Extract it and install `app-debug.apk` on an Android phone.

The workflow uses the current `android-actions/setup-android@v4` action and does not request the removed Android SDK `tools` package.

The repository's Gradle wrapper is intentionally not required by the workflow; GitHub installs Gradle 8.7 directly.
