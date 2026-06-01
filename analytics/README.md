# FirebaseKit Analytics

`analytics` exposes Firebase Analytics event logging and user identity APIs to Kotlin Multiplatform code. Android and iOS use the native Firebase SDKs, Web uses the Firebase JS SDK, and Desktop/JVM sends events through Google Analytics Measurement Protocol.

## Installation

Add the module to the shared source set that needs Analytics:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:analytics:0.3.1")
        }
    }
}
```

For iOS targets, run the linkage package integration after adding the dependency:

```bash
XCODEPROJ_PATH="$(pwd)/sample/iosApp/iosApp.xcodeproj" ./gradlew -p "$(pwd)" ':sample:shared:integrateLinkagePackage'
```

## Platform Setup

Initialize Firebase once before accessing `Firebase.analytics`.

### Android

Analytics uses the native Firebase Android SDK.

Add the Google Services plugin to the Android app module and place `google-services.json` in that app module:

```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

Initialize Firebase from your Android entry point:

```kotlin
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
    }
}
```

### iOS

Analytics uses the native Firebase iOS SDK through Swift Package Manager integration.

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

### Web (JS and Wasm)

Analytics uses the Firebase Web Analytics SDK.

Initialize Firebase with the full web app config. `measurementId` is required by the current Web initialization API:

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

### Desktop (JVM)

Desktop Analytics uses the Google Analytics Measurement Protocol endpoint directly from the JVM app.

Initialize Firebase before creating the UI. `measurementId` and `measurementProtocolApiSecret` are required for Analytics event uploads:

```kotlin
Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    projectId = "your-project-id",
    appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
    authDomain = "your-project.firebaseapp.com",
    storageBucket = "your-project.firebasestorage.app",
    messagingSenderId = "111111111111",
    measurementId = "G-AAAAAAAAAA",
    measurementProtocolApiSecret = "your-measurement-protocol-secret",
    interval = 60.minutes,
    cacheFilePath = "cache/firebase_data",
)
```

## Common Usage

Access Analytics through `Firebase.analytics`:

```kotlin
Firebase.analytics.setAnalyticsCollectionEnabled(true)

Firebase.analytics.logEvent(
    name = "purchase",
    parameters = Bundle().apply {
        put("item_id", "pizza")
        put("quantity", 1)
        put("price", 12.5)
        put("featured", true)
    }
)

Firebase.analytics.setUserId("user-42")
Firebase.analytics.setUserProperty("favorite_food", "pizza")
Firebase.analytics.resetAnalyticsData()
```

`Bundle` supports primitive values and JSON payloads:

```kotlin
@Serializable
data class PurchaseMeta(
    val coupon: String,
    val source: String,
)

Firebase.analytics.logEvent(
    name = "purchase_meta",
    parameters = Bundle().apply {
        put("currency", "USD")
        put("meta", PurchaseMeta(coupon = "SPRING", source = "banner"), PurchaseMeta.serializer())
    }
)
```

Supported `Bundle.put(...)` value types are `String`, `Long`, `Int`, `Double`, `Float`, `Boolean`, `JsonElement`, and values paired with a `KSerializer<T>`.

## Target Notes

- Android and iOS pass events, user IDs, user properties, collection settings, and reset calls to the native Firebase Analytics SDKs.
- Web uses `getAnalytics`, `logEvent`, `setAnalyticsCollectionEnabled`, `setUserId`, and `setUserProperties` from `firebase/analytics`.
- `resetAnalyticsData()` is currently a no-op on Web because the Firebase Web Analytics SDK does not expose an equivalent reset API.
- JVM/Desktop keeps `setUserId(...)` and `setUserProperty(...)` values in memory and includes them with later Measurement Protocol events.
- JVM/Desktop `resetAnalyticsData()` clears the in-memory user ID and user properties; it does not clear Google Analytics server-side data.

## Sample App

The shared sample logs `sample_app_open` from `sample/shared/src/commonMain/kotlin/com/firebasekit/sample/AnalyticsViewModel.kt`:

```kotlin
Firebase.analytics.setAnalyticsCollectionEnabled(true)
Firebase.analytics.logEvent(
    name = "sample_app_open",
    parameters = Bundle().apply {
        put("platform", Platform.current().name)
    }
)
```