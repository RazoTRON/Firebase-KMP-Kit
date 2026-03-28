# Firebase KMP Kit

A **Kotlin Multiplatform** library that provides **Firebase Remote Config SDK** across all major platforms **(Android, iOS, JS, WASM, Desktop)** through a single, unified API.

## Supported Targets

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
            implementation("com.firebasekit.remoteconfig:remote-config:0.0.1")
        }
    }
}
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

## Platform Setup

Each platform requires a one-time `Firebase.initialize()` call before accessing `Firebase.remoteConfig`.

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
    Firebase.initialize(interval = 60.minutes) // optional fetch throttle
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
