# FirebaseKit Remote Config

`remote-config` exposes Firebase Remote Config to Kotlin Multiplatform code. Android and iOS use the native Firebase SDKs, Web uses the Firebase JS SDK, and Desktop/JVM calls Firebase Remote Config through REST with a cached Firebase Installation ID.

## Installation

Add the module to the shared source set that needs Remote Config:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:remote-config:0.4.0")
        }
    }
}
```

For iOS targets, run the linkage package integration after adding the dependency:

```bash
XCODEPROJ_PATH="$(pwd)/sample/iosApp/iosApp.xcodeproj" ./gradlew -p "$(pwd)" ':sample:shared:integrateLinkagePackage'
```

## Platform Setup

### Android

Remote Config uses the native Firebase Android SDK.

Add the Google Services plugin to the Android app module and place `google-services.json` in that app module:

```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

Initialize Firebase from your Android entry point before reading `Firebase.remoteConfig`:

```kotlin
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
    }
}
```

### iOS

Remote Config uses the native Firebase iOS SDK.

Add `GoogleService-Info.plist` to the iOS app target, then initialize Firebase from Kotlin shared code:

```kotlin
// Example file: shared/iosMain/Firebase.kt

fun ConfigureFirebase() {
    Firebase.initialize()
}
```

Call that function from the iOS app startup path before rendering shared UI:

```swift
import shared

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseKt.ConfigureFirebase()
    }
}
```

iOS also exposes a platform-specific helper for the Firebase Remote Config minimum fetch interval:

```kotlin
// iosMain

Firebase.remoteConfig.setConfigSettings(interval = 30.minutes)
```

### Web (JS and Wasm)

Remote Config uses the Firebase Web SDK. Initialize Firebase with the full web app config before reading `Firebase.remoteConfig`:

```kotlin
Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    authDomain = "your-project.firebaseapp.com",
    projectId = "your-project-id",
    storageBucket = "your-project.firebasestorage.app",
    messagingSenderId = "111111111111",
    appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
    measurementId = "G-AAAAAAAAAA",
)
```

The web target wraps the Firebase JS SDK through the module npm dependency.

### Desktop (JVM)

Desktop Remote Config uses Firebase REST APIs through Ktor. Initialize Firebase before creating the UI. Remote Config requires `apiKey`, `projectId`, and `appId`:

```kotlin
Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    projectId = "your-project-id",
    appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
    authDomain = "your-project.firebaseapp.com",
    storageBucket = "your-project.firebasestorage.app",
    interval = 60.minutes,
    cacheFilePath = "cache/firebase_data",
)
```

`Firebase.remoteConfig.fetchAndActivate()` fetches once, then starts an automatic refresh loop using the `interval` passed to `Firebase.initialize()`; the default interval is 60 minutes.

## Common Usage

Initialize Firebase once before reading `Firebase.remoteConfig`. Fetching is suspending, so call it from a coroutine:

```kotlin
Firebase.remoteConfig.fetchAndActivate()
```

Read typed values by key:

```kotlin
val enabled: Boolean? = Firebase.remoteConfig.getBoolean("feature_enabled")
val title: String? = Firebase.remoteConfig.getString("welcome_title")
val ratio: Double? = Firebase.remoteConfig.getDouble("rollout_ratio")
val count: Long? = Firebase.remoteConfig.getLong("max_items")
val limit: Int? = Firebase.remoteConfig.getInt("daily_limit")
```

Export all active values as a JSON string:

```kotlin
val json: String? = Firebase.remoteConfig.allToJson()
```

The sample common UI uses this pattern in `sample/shared/src/commonMain/kotlin/com/firebasekit/sample/RemoteConfigViewModel.kt`.
