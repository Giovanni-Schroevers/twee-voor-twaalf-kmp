This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Prerequisites

- **JDK 17+** (the Gradle toolchain will download a matching JDK if one is missing).
- **Android Studio** (latest stable) with the **Android SDK** — for the Android app and as the recommended IDE.
- **Xcode** (macOS only) — only needed to run the iOS app.

### Setup

1. Clone the repo and open it in Android Studio:
   ```sh
   git clone <repo-url>
   cd tweevoortwaalfkmp
   ```
2. Android Studio writes `local.properties` (with your `sdk.dir`) on first sync. If you build from the
   command line instead, create it yourself:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   ```
3. (Optional) Point the app at a backend. With no config it talks to a **local** backend on `localhost`
   (the Android emulator uses `10.0.2.2`), so just run the `2-voor-12` backend alongside it. To use a
   deployed backend instead, set the `backend.url` Gradle property (must end with a trailing slash) in
   `gradle.properties`, `~/.gradle/gradle.properties`, or per build:
   ```sh
   ./gradlew :desktopApp:run -Pbackend.url=https://your-backend.example.com/
   ```
   The backend URL can also be changed at runtime from the in-app Settings screen.
4. Build everything to verify the setup:
   ```sh
   ./gradlew build
   ```

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Desktop tests: `./gradlew :shared:jvmTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…