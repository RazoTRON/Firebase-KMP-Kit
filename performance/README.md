# FirebaseKit Performance

`performance` exposes Firebase Performance Monitoring custom traces and HTTP metrics to Kotlin Multiplatform code. Android and iOS use the native Firebase SDKs, Web uses the Firebase JS SDK, and Desktop/JVM sends custom performance events through Firebase's logging endpoint.

## Installation

Add the module to the shared source set that needs Performance Monitoring:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:performance:0.4.0")
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

Performance uses the native Firebase Android SDK.

Add the Google Services plugin to the Android app module and place `google-services.json` in that app module:

```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

Initialize Firebase from your Android entry point before accessing `Firebase.performance`:

```kotlin
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
    }
}
```

### iOS

Performance uses the native Firebase iOS SDK through Swift Package Manager integration.

Add `GoogleService-Info.plist` to the iOS app target and initialize Firebase from Kotlin shared code:

```kotlin
// Example file: shared/iosMain/Firebase.kt

fun ConfigureFirebase() {
    Firebase.initialize()
}
```

Call that function from the iOS app startup path before recording traces or HTTP metrics.

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

iOS HTTP metrics support these methods: `GET`, `PUT`, `POST`, `DELETE`, `HEAD`, `PATCH`, `OPTIONS`, `TRACE`, and `CONNECT`. Passing an unsupported method or an invalid URL fails when the metric is created.

### Web (JS and Wasm)

Performance uses the Firebase Web Performance SDK.

Initialize Firebase with the full web app config:

```kotlin
fun main() {
    Firebase.initialize(
        apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
        authDomain = "your-project.firebaseapp.com",
        projectId = "your-project-id",
        storageBucket = "your-project.firebasestorage.app",
        messagingSenderId = "111111111111",
        appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
        measurementId = "G-AAAAAAAAAA",
    )
}
```

Web custom traces use the native Firebase Web trace API. `newHttpMetric(...)` is represented as a custom trace named `http_metric` with HTTP details stored as attributes and metrics, because the Firebase Web SDK does not expose the same native `HttpMetric` type used by Android and iOS.

### Desktop (JVM)

Desktop Performance uses the FirebaseKit JVM core identity and posts custom performance events through a Ktor HTTP client.

Initialize Firebase before creating traces or metrics:

```kotlin
Firebase.initialize(
    apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
    projectId = "your-project-id",
    appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
    authDomain = "your-project.firebaseapp.com",
    storageBucket = "your-project.firebasestorage.app",
    messagingSenderId = "111111111111",
    measurementId = "G-AAAAAAAAAA",
)
```

## Common Usage

Initialize Firebase once before reading `Firebase.performance`.

```kotlin
Firebase.performance.setPerformanceCollectionEnabled(true)
```

### Custom Traces

Use custom traces to measure app work that is not already covered by platform instrumentation:

```kotlin
val trace = Firebase.performance.newTrace("checkout_flow")

trace.start()
trace.putAttribute("source", "cart")
trace.incrementMetric("items")
trace.putMetric("total_cents", 1299)
try {
    // Work you want to measure.
} finally {
    trace.stop()
}
```

The common trace API supports:

- `start()`
- `stop()`
- `incrementMetric(name, by)`
- `putMetric(name, value)`
- `getMetric(name)`
- `putAttribute(name, value)`
- `getAttribute(name)`
- `getAttributes()`
- `removeAttribute(name)`

### HTTP Metrics

Use HTTP metrics when you want to report network timing and response details explicitly:

```kotlin
val metric = Firebase.performance.newHttpMetric(
    url = "https://example.com/products",
    httpMethod = "GET",
)

metric.start()
try {
    // Execute the network request here.
    metric.setHttpResponseCode(200)
    metric.setRequestPayloadSize(0)
    metric.setResponsePayloadSize(2048)
    metric.setResponseContentType("application/json")
} finally {
    metric.stop()
}
```

The common HTTP metric API supports:

- `start()`
- `stop()`
- `setRequestPayloadSize(bytes)`
- `setResponsePayloadSize(bytes)`
- `setHttpResponseCode(code)`
- `setResponseContentType(contentType)`
- `putAttribute(name, value)`
- `getAttribute(name)`
- `getAttributes()`
- `removeAttribute(name)`

## Sample

The shared sample records both a custom trace and an HTTP metric from `sample/shared/src/commonMain/kotlin/com/firebasekit/sample/PerformanceViewModel.kt`.
