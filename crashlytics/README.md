# FirebaseKit Crashlytics

FirebaseKit `crashlytics` exposes Firebase crash reporting SDK to Kotlin Multiplatform code through `Firebase.crashlytics`.

This module is **Experimental**. Android and iOS use the native Firebase Crashlytics SDKs. Web (JS/Wasm) and Desktop/JVM are not supported because Firebase does not provide Crashlytics SDKs for any of those targets.

## Installation

Add the module to the shared source set that needs Crashlytics:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.razotron.firebase-kit:crashlytics:0.4.0")
        }
    }
}
```

For iOS targets, run the linkage package integration after adding the dependency:

```bash
XCODEPROJ_PATH="$(pwd)/sample/iosApp/iosApp.xcodeproj" ./gradlew -p "$(pwd)" ':sample:shared:integrateLinkagePackage'
```

## Platform Setup

Initialize Firebase once before accessing `Firebase.crashlytics`.

### Android

Crashlytics uses the native Firebase Android SDK.

Add the Google Services plugin and the Firebase Crashlytics Gradle plugin to the Android app module, then place `google-services.json` in that app module:

```kotlin
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}
```

Initialize Firebase from your Android entry point and enable Crashlytics collection when your app is ready to collect reports:

```kotlin
class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
    }
}
```

### iOS

Crashlytics uses the native Firebase iOS SDK through Swift Package Manager integration.

Add `GoogleService-Info.plist` to the iOS app target.

Crashlytics needs a dSYM upload run script in the Xcode build phases. Add a "Run Script" phase at the END of build phases:

```bash
"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
```

With input files:
```
${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}
${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${PRODUCT_NAME}
${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Info.plist
$(TARGET_BUILD_DIR)/$(UNLOCALIZED_RESOURCES_FOLDER_PATH)/GoogleService-Info.plist
$(TARGET_BUILD_DIR)/$(EXECUTABLE_PATH)
```

Also set **Debug Information Format** to `DWARF with dSYM File` for all build configurations.

Initialize Firebase from Kotlin shared code and enable Crashlytics collection:

```kotlin
// Example file: shared/iosMain/Firebase.kt

fun ConfigureFirebase() {
    Firebase.initialize()
    Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
}
```

Call that function from the iOS app startup path before using Crashlytics APIs:

```swift
import shared

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseKt.ConfigureFirebase()
        return true
    }
}
```

### Web (JS and Wasm) / Desktop (JVM)

Crashlytics is not supported on Web and Desktop targets. Firebase does not provide an official Crashlytics SDK for any of those targets.

## Common Usage

Access Crashlytics through `Firebase.crashlytics`:

```kotlin
Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)

Firebase.crashlytics.setUserId("user-42")
Firebase.crashlytics.setCustomKey("screen", "checkout")
Firebase.crashlytics.setCustomKey("retrying", false)

Firebase.crashlytics.log("Checkout started")
Firebase.crashlytics.recordException(IllegalStateException("Payment failed"))
```

Set multiple custom keys with `CrashlyticsKeys`:

```kotlin
Firebase.crashlytics.setCustomKeys(
    CrashlyticsKeys().apply {
        put("screen", "checkout")
        put("retrying", false)
        put("items", 2)
        put("total", 12.5)
    }
)
```

Attach keys to a recorded exception:

```kotlin
Firebase.crashlytics.recordException(
    throwable = IllegalStateException("Payment failed"),
    keys = CrashlyticsKeys().apply {
        put("screen", "checkout")
        put("fatal", false)
    },
)
```

The common API supports:

- `setCrashlyticsCollectionEnabled(enabled)`
- `didCrashOnPreviousExecution()`
- `setUserId(userId)`
- `setCustomKey(key, value)` for `String`, `Boolean`, `Double`, `Float`, `Int`, and `Long`
- `setCustomKeys(keys)`
- `log(message)`
- `recordException(throwable)`
- `recordException(throwable, keys)`
- `sendUnsentReports()`
- `deleteUnsentReports()`

## Limitations and Known Issues
- This module is Experimental. Crashlytics is only backed by native SDKs on Android and iOS.
- On iOS, Kotlin/Native fatal crashes can appear in Crashlytics with the generic fatal name `Fatal Exception: (anonymous namespace)::ExceptionObjHolderImpl`. Kotlin frames and Kotlin stacktrace logs may still be present in the report, but Crashlytics can keep the fatal classification generic instead of showing the original Kotlin exception type. See Kotlin Slack discussions: [Kotlin/Native iOS Crashlytics report with `ExceptionObjHolderImpl`](https://slack-chats.kotlinlang.org/t/527213/i-m-getting-a-crash-in-my-ios-app-but-i-have-no-idea-what-it) and [KMP/CMP Crashlytics confirmation discussion](https://slack-chats.kotlinlang.org/t/33344589/hey-all-wave-looking-for-confirmation-from-anyone-running-km).
- Web JS/Wasm and Desktop/JVM are unsupported no-op targets because there is no Firebase Crashlytics SDK for those platforms.

## Target Notes
- Android delegates all common APIs to the native Firebase Crashlytics Android SDK.
- iOS delegates to the native Firebase Crashlytics SDK through Swift Package Manager and Kotlin/Native interop.

