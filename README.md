# 🔥 Firebase KMP Kit
![Static Badge](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20JS%20%7C%20WASM%20%7C%20Desktop-brightgreen)
![Static Badge](https://img.shields.io/badge/Kotlin-2.4.0-violet)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

A **Kotlin Multiplatform** library that provides **Firebase Services** across all major platforms **(Android, iOS, JS, WASM, Desktop)** through a single, unified API.

## Characteristics

- 🚫 **No CocoaPods** under the hood — implemented entirely with Kotlin/Native interop.
- 🌐 Supports all major Kotlin Multiplatform targets: `Android`, `iOS`, `JS`, `WASM`, and `Desktop`.
- ⚙️ API fully aligned with the official Firebase SDK.
- 🔄 Built with **Kotlin Coroutines** and **Flow** for reactive and asynchronous programming.
- 🔗 Uses **Kotlinx Serialization** for consistent cross-platform JSON handling.

## Supported Targets

| Module              | Android | iOS | JS | Wasm | Desktop | Description                                                                                                                         |
|---------------------|:-------:|:---:|:--:|:----:|:-------:|-------------------------------------------------------------------------------------------------------------------------------------|
| `analytics`         |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase Analytics event logging and user properties                                                                                |
| `core`              |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase instance                                                                                                                   |
| `crashlytics`       |    ✅    |  ✅  | -  |  -   |    -    | Experimental Firebase Crashlytics crash reporting for mobile targets. There is no official Firebase SDK for Web or Desktop targets. |
| `messaging`         |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase Cloud Messaging token APIs; Desktop uses a browser bridge                                                                  |
| `performance`       |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase Performance Monitoring custom traces and HTTP metrics                                                                      |
| `remote-config`     |    ✅    |  ✅  | ✅  |  ✅   |    ✅    | Firebase Remote Config                                                                                                              |

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
            implementation("io.github.razotron.firebase-kit:analytics:0.4.0")
            implementation("io.github.razotron.firebase-kit:remote-config:0.4.0")
            implementation("io.github.razotron.firebase-kit:messaging:0.4.0")
            implementation("io.github.razotron.firebase-kit:performance:0.4.0")
            implementation("io.github.razotron.firebase-kit:crashlytics:0.4.0")
        }
    }
}
```

## Analytics - Common API

Android, iOS, JS, and Wasm expose `Firebase.analytics` for Firebase Analytics event logging:

```kotlin
// commonMain

Firebase.analytics.logEvent(
    name = "purchase",
    parameters = Bundle().apply {
        put("item_id", "pizza")
        put("quantity", 1)
        put("price", 12.5)
        put("is_featured", true)
    }
)

Firebase.analytics.setUserId("user-42")
Firebase.analytics.setUserProperty("favorite_food", "pizza")
Firebase.analytics.setAnalyticsCollectionEnabled(true)
Firebase.analytics.resetAnalyticsData()
```

`Bundle` also supports serializable payloads via `kotlinx.serialization`:

```kotlin
// commonMain

@Serializable
data class PurchaseMeta(
    val coupon: String,
    val source: String,
)

Firebase.analytics.logEvent(
    name = "purchase_meta",
    parameters = Bundle().apply {
        put("meta", PurchaseMeta(coupon = "SPRING", source = "banner"), PurchaseMeta.serializer())
    }
)
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

## Messaging

```kotlin
// commonMain

// Read the current default FCM registration token
val token: String = Firebase.messaging.getToken()

// Delete the current default FCM registration token
Firebase.messaging.deleteToken()
```

Android and iOS also expose topic subscription APIs from their platform source sets. Web exposes foreground `onMessage(...)`, and Desktop uses a local browser bridge for token creation and message delivery.

See the full [Messaging module guide](messaging/README.md) for installation, platform setup, target differences, and sample app usage.

## Performance

```kotlin
// commonMain

val trace = Firebase.performance.newTrace("checkout_flow")
trace.start()
trace.putAttribute("source", "cart")
trace.incrementMetric("items")
trace.stop()

val metric = Firebase.performance.newHttpMetric("https://example.com/products", "GET")
metric.start()
metric.setHttpResponseCode(200)
metric.setResponsePayloadSize(2048)
metric.stop()
```

See the full [Performance module guide](performance/README.md) for installation, platform setup, target differences, and sample app usage.

## Crashlytics - Experimental

`crashlytics` exposes Firebase Crashlytics crash reporting APIs through `Firebase.crashlytics`. This feature is **Experimental** and currently backed by native SDKs on Android and iOS only.

There is no Firebase Crashlytics SDK for Web (JS/Wasm) or Desktop targets.

```kotlin
// commonMain

Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
Firebase.crashlytics.setUserId("user-42")
Firebase.crashlytics.setCustomKey("screen", "checkout")
Firebase.crashlytics.log("Checkout started")
Firebase.crashlytics.recordException(throwable)
```

See the full [Crashlytics module guide](crashlytics/README.md) for installation, platform setup, target differences, and sample app usage.

## Platform Setup

Each platform requires a one-time `Firebase.initialize()` call before accessing Firebase services.

### Android

Add the dependency and the Google Services plugin to your app module:

```kotlin
// build.gradle.kts of android target module
plugins {
    id("com.google.gms.google-services")
}
```

Place your `google-services.json` in the `app` module of android target, then initialize:

```kotlin
// androidMain

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
    }
}
```

### iOS

Add a `GoogleService-Info.plist` to your app target.

Initialize from Kotlin shared code:

```kotlin
// iosMain

fun FirebaseConfigure() {
    Firebase.initialize()
}
```

Call from Swift:

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

### Web (JS / Wasm)

Initialize with your full Firebase web config:

```kotlin
// webMain/jsMain/wasmMain

Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    projectId = "your-project-id",
    appId = "1.1111111111:web:AAAAAAAAAAAAAAAAA",
    authDomain = "your-project-firebase.firebaseapp.com",
    storageBucket = "your-project-firebase.firebasestorage.app",
    messagingSenderId = "11111111111111",
    measurementId = "A-AAAAAAAAAA",
)
```

### Desktop (JVM)

Initialize before creating the UI:

```kotlin
// desktopMain

Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    projectId = "your-project-id",
    appId = "1.1111111111:web:AAAAAAAAAAAAAAAAA",
    authDomain = "your-project-firebase.firebaseapp.com", // optional, defaults from projectId
    storageBucket = "your-project-firebase.firebasestorage.app", // optional
    messagingSenderId = "11111111111111", // required for messaging
    webVapidKey = "YOUR_WEB_PUSH_CERTIFICATE_KEY_PAIR", // required for messaging
    measurementProtocolApiSecret = "your-measurement-protocol-secret", // optional, required for analytics
    cacheFilePath = "cache/firebase_data" // optional: FID cache location
)
```

The JVM target connects to Firebase via REST APIs using Ktor. Remote Config requires your Firebase project's API key, project ID, and app ID. Analytics uses GA4 Measurement Protocol and also requires a Measurement Protocol API secret from Google Analytics.

The desktop implementation automatically re-fetches config on the specified interval (defaults to 60 minutes). A Firebase Installation ID (FID) is generated and cached locally at `cacheFilePath`.

Desktop Messaging uses a local loopback browser bridge. See the [Messaging module guide](messaging/README.md) for token caching, foreground message handling, and target-specific setup.

## License

See [LICENSE](LICENSE) for details.
