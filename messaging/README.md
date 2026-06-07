# FirebaseKit Messaging

`messaging` exposes Firebase Cloud Messaging token APIs to Kotlin Multiplatform code. Android and iOS use the native Firebase SDKs, Web uses the Firebase JS SDK, and Desktop/JVM uses a local browser bridge to register with Firebase Cloud Messaging.

## Installation

Add the module to the shared source set that needs Messaging:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:messaging:0.4.0")
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

Messaging uses the native Firebase Android SDK.

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

Token APIs work after initialization. To display received push notifications, the Android app still owns the notification UI pieces:

- Add `android.permission.POST_NOTIFICATIONS` for Android 13+.
- Request the runtime notification permission on Android 13+.
- Create a notification channel on Android 8+.
- Register a `FirebaseMessagingService` in `AndroidManifest.xml` if you need foreground/background message handling.

The sample shows this in `sample/androidApp/src/main/kotlin/com/firebasekit/sample/androidApp/AppActivity.kt`, `SampleMessagingService.kt`, and `sample/androidApp/src/main/AndroidManifest.xml`.

### iOS

Messaging uses the native Firebase iOS SDK through Swift Package Manager integration.

Add `GoogleService-Info.plist` to the iOS app target, enable Push Notifications, and enable the Remote notifications background mode if your app handles background pushes.

Initialize Firebase from Kotlin shared code:

```kotlin
// Example file: shared/iosMain/Firebase.kt

fun ConfigureFirebase() {
    Firebase.initialize()
}
```

Call that function from the iOS app startup path, request notification authorization, and register for remote notifications in Swift:

```swift
func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
) -> Bool {
    FirebaseKt.ConfigureFirebase()

    let center = UNUserNotificationCenter.current()
    center.delegate = self
    center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
        guard granted else { return }
        DispatchQueue.main.async {
            application.registerForRemoteNotifications()
        }
    }

    return true
}
```

Set the APNs token to Firebase Messaging when registration succeeds.

```kotlin
// Example file: shared/iosMain/Firebase.kt

fun SetApnsToken(deviceToken: NSData) {
    Firebase.setApnsToken(deviceToken)
}
```

```swift
func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
) {
    Messaging.messaging().apnsToken = deviceToken
    FirebaseKt.SetApnsToken(deviceToken: deviceToken)
}
```

On iOS, `Firebase.messaging.getToken()` waits for that APNs token before requesting the FCM token. If APNs registration never completes, the call fails with a setup error instead of asking Firebase Messaging too early.

### Web (JS and Wasm)

Messaging uses the Firebase Web Messaging SDK.

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

Request browser notification permission before creating a token:

```kotlin
Firebase.messaging.getToken()
```

Foreground messages can be observed while the page is open:

```kotlin
Firebase.messaging.onMessage { payload ->
    val title = payload.notification?.title
}
```

Web token retrieval requires a service worker at `/firebase-messaging-sw.js`.

Use the FirebaseKit Generate JS Resource Gradle plugin in the Web app module to generate that service worker and package it with the JS/Wasm resources:

```kotlin
plugins {
    id("io.github.razotron.firebasekit.generate-js-resource")
}

generateJsResources(
    apiKey = properties.getProperty("FIREBASE_API_KEY"),
    authDomain = properties.getProperty("FIREBASE_AUTH_DOMAIN"),
    projectId = properties.getProperty("FIREBASE_PROJECT_ID"),
    storageBucket = properties.getProperty("FIREBASE_STORAGE_BUCKET"),
    messagingSenderId = properties.getProperty("FIREBASE_MESSAGING_SENDER_ID"),
    appId = properties.getProperty("FIREBASE_APP_ID"),
    measurementId = properties.getProperty("FIREBASE_MEASUREMENT_ID"),
)
```

The plugin registers a `generateJsResource` task, writes `firebase-messaging-sw.js` under the generated Web resources directory, and makes JS/Wasm resource processing depend on that task. The generated service worker initializes Firebase Web Messaging with the supplied config values.

Web supports `getToken()`, `deleteToken()`, and foreground `onMessage(...)`. Topic subscription APIs are not available on Web.

### Desktop (JVM)

Desktop Messaging uses Firebase Web Messaging through a local loopback browser bridge.

Initialize Firebase before creating the UI. `messagingSenderId` and `webVapidKey` are required for Messaging:

```kotlin
suspend fun configureFirebase() {
    Firebase.initialize(
        apiKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAA-AAAAAAAAAAA",
        projectId = "your-project-id",
        appId = "1:111111111111:web:aaaaaaaaaaaaaaaaaaaaaa",
        authDomain = "your-project.firebaseapp.com",
        storageBucket = "your-project.firebasestorage.app",
        messagingSenderId = "111111111111",
        webVapidKey = "YOUR_WEB_PUSH_CERTIFICATE_KEY_PAIR",
        measurementId = "G-AAAAAAAAAA",
        measurementProtocolApiSecret = "your-measurement-protocol-secret",
        interval = 60.minutes,
        cacheFilePath = "cache/firebase_data",
    )

    Firebase.messaging.refreshTokenDuration = 30.days
    Firebase.messaging.cacheTokenPath = "cache/firebase_messaging_token"
}
```

Calling `Firebase.messaging.getToken()` starts a small HTTP server, opens the system browser to `http://127.0.0.1:45777`, registers Firebase Cloud Messaging through the Firebase JS SDK, and returns the token to the JVM app.

Desktop supports `getToken()`, `deleteToken()`, foreground `onMessage(...)`, `refreshTokenDuration`, and `cacheTokenPath`. Topic subscription APIs are not available on Desktop.

Receive push notifications callback (desktopMain):
```kotlin
Firebase.messaging.onMessage { payloadJson -> ... }
```

## Common Usage

Initialize Firebase once before reading `Firebase.messaging`. The common API supports token creation and token deletion:

```kotlin
Firebase.messaging.getToken()
Firebase.messaging.deleteToken()
```

Topic subscription APIs are platform-specific in this module. They are available from **Android** and **iOS** source sets:

```kotlin
Firebase.messaging.subscribeToTopic("news")
Firebase.messaging.unsubscribeFromTopic("news")
```
