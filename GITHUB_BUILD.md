# GitHub APK build

KeepJ includes a GitHub Actions workflow at `.github/workflows/build.yml`.

It builds the debug APK with:

- JDK 17
- Android SDK 35
- Build Tools 35.0.0
- Gradle 8.7
- Android Gradle Plugin 8.5.2

The workflow intentionally uses the Gradle installation provided by `gradle/actions/setup-gradle` instead of the bundled `gradlew` wrapper. The wrapper JAR currently included in this project is incomplete and is missing `org.gradle.wrapper.IDownload`.

## Build it on GitHub

1. Push the project to GitHub.
2. Open **Actions**.
3. Select **Build Android APK**.
4. Run the workflow manually with **Run workflow**, or push to `main`.
5. Open the completed workflow run.
6. Download the **keepj-debug-apk** artifact.

The resulting `app-debug.apk` is a normal debug APK and can be installed on an Android device with USB/ADB or an APK installer.
