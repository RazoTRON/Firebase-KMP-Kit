# 🔥 Firebase KMP Kit
![Static Badge](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20JS%20%7C%20WASM%20%7C%20Desktop-brightgreen)
![Static Badge](https://img.shields.io/badge/Kotlin-2.3.0-violet)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

A **Kotlin Multiplatform** library that provides **Firebase Services** across all major platforms **(Android, iOS, JS, WASM, Desktop)** through a single, unified API.

## Characteristics

- 🚫 **No CocoaPods** under the hood — implemented entirely with Kotlin/Native interop.
- 🌐 Supports all major Kotlin Multiplatform targets: `Android`, `iOS`, `JS`, `WASM`, and `Desktop`.
- ⚙️ API fully aligned with the official Firebase SDK.
- 🔄 Built with **Kotlin Coroutines** and **Flow** for reactive and asynchronous programming.
- 🔗 Uses **Kotlinx Serialization** for consistent cross-platform JSON handling.
- ✅ Comprehensive test coverage across all supported platforms.

## Supported Targets

| Module          | Android | iOS | JS | Wasm | Desktop | Description                                                   |
|-----------------|:-------:|:---:|:--:|:----:|:-------:|---------------------------------------------------------------|
| `core`          |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase instance                                    |
| `messaging`     |    ✅    |  ✅  | ❌  |  ❌   |    ❌    | Firebase Cloud Messaging token + topic APIs         |
| `remote-config` |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Remote Config |

### KMP Target Names

| Platform | Target |
|----------|--------|
| Android | `androidTarget` |
| iOS | `iosX64`, `iosArm64`, `iosSimulatorArm64` |
| Desktop (JVM) | `jvm` |
| Web (JS) | `js(browser)` |
| Web (Wasm) | `wasmJs(browser)` |

## Dependency

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:remote-config:0.2.0-rc3")
            implementation("io.github.razotron.firebase-kit:messaging:0.2.0-rc3")
        }
    }
}
```

## Installation (only iOS targets)

```bash
// Change path to the 'xcodeproj' file and module with Firebase Kit dependency if needed and perform
XCODEPROJ_PATH="$(pwd)/iosApp/iosApp.xcodeproj" ./gradlew -p "$(pwd)" ':shared:integrateLinkagePackage'
```

## Remote Config - Common API

All platforms share the same `FirebaseRemoteConfig` interface, accessed via `Firebase.remoteConfig`:

```kotlin
// Fetch latest config values from Firebase and activate them
Firebase.remoteConfig.fetchAndActivate()

// Read typed values by key
val flag: Boolean?  = Firebase.remoteConfig.getBoolean("key")
val label: String?  = Firebase.remoteConfig.getString("key")
val price: Double?  = Firebase.remoteConfig.getDouble("key")
val count: Long?    = Firebase.remoteConfig.getLong("key")
val limit: Int?     = Firebase.remoteConfig.getInt("key")

// Export all config as a JSON string
val json: String?   = Firebase.remoteConfig.allToJson()
```

`fetchAndActivate()` is a `suspend` function -- call it from a coroutine scope:

```kotlin
val remoteConfigData = flow {
    Firebase.remoteConfig.fetchAndActivate()
    emit(Firebase.remoteConfig.allToJson())
}.catch { emit("Error: ${it.message}") }
```

## Messaging - Common API

Android and iOS share the same `FirebaseMessaging` interface, accessed via `Firebase.messaging`:

```kotlin
// Read the current default FCM registration token
val token: String = Firebase.messaging.getToken()

// Delete the current default FCM registration token
Firebase.messaging.deleteToken()

// Manage topic subscriptions
Firebase.messaging.subscribeToTopic("news")
Firebase.messaging.unsubscribeFromTopic("news")
```

All Messaging operations are `suspend` functions.

## Platform Setup

Each platform requires a one-time `Firebase.initialize()` call before accessing `Firebase.remoteConfig` or `Firebase.messaging`.

### Android

Add the dependency and the Google Services plugin to your app module:

```kotlin
// build.gradle.kts
plugins {
    id("com.google.gms.google-services")
}
```

Place your `google-services.json` in the `app` module, then initialize:

```kotlin
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
    }
}
```

### iOS

Add the Firebase iOS SDK to your Xcode project (via SPM or CocoaPods), then add a `GoogleService-Info.plist` to your app target.

Initialize from Kotlin shared code:

```kotlin
// shared/src/iosMain
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize

fun Configure() {
    Firebase.initialize()
}
```

Call from Swift:

```swift
import shared

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        FirebaseKt.Configure()
        return MainKt.MainViewController()
    }
}
```

### Web (JS / Wasm)

Initialize with your full Firebase web config:

```kotlin
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize

fun main() {
    Firebase.initialize(
        apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
        projectId = "your-project-id",
        appId = "1.1111111111:web:AAAAAAAAAAAAAAAAA",
        authDomain = "your-project-firebase.firebaseapp.com",
        storageBucket = "your-project-firebase.firebasestorage.app",
        messagingSenderId = "11111111111111",
        measurementId = "A-AAAAAAAAAA",
    )
}
```
**Note:**
> The web target wraps the Firebase JS SDK (`firebase@10.13.2`).

### Messaging Notes

#### Android

Messaging uses the native Firebase Android SDK. The same `google-services` setup and `Firebase.initialize(this)` call shown above are enough for token and topic operations.

#### iOS

Messaging uses the native Firebase iOS SDK. Your app target still owns notification permission requests, APNs capability setup, and `registerForRemoteNotifications()`.

If you disable Firebase Messaging method swizzling, forward the APNs device token manually:

```kotlin
import com.firebasekit.messaging.setFirebaseMessagingApnsToken
import platform.Foundation.NSData

fun forwardApnsToken(token: NSData) {
    setFirebaseMessagingApnsToken(token)
}
```

The current `messaging` module does not abstract foreground/background message delivery delegates.

### Desktop (JVM)

Initialize before creating the UI:

```kotlin
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize

fun main() {
    Firebase.initialize(
        apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
        projectId = "your-project-id",
        appId = "1.1111111111:web:AAAAAAAAAAAAAAAAA",
        interval = 60.minutes,           // optional: auto-refresh interval
        cacheFilePath = "cache/firebase_data" // optional: FID cache location
    )
}
```

The JVM target connects to Firebase via the REST API using Ktor. It requires your Firebase project's API key, project ID, and app ID.

The desktop implementation automatically re-fetches config on the specified interval (defaults to 60 minutes). A Firebase Installation ID (FID) is generated and cached locally at `cacheFilePath`.

## License

See [LICENSE](LICENSE) for details.
