import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.crashlytics.crashlytics
import com.firebasekit.messaging.setApnsToken
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import kotlin.experimental.ExperimentalNativeApi

fun Configure() {
    Firebase.initialize()
}

@OptIn(ExperimentalForeignApi::class)
fun SetApnsToken(deviceToken: NSData) {
    Firebase.setApnsToken(deviceToken)
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@Suppress("unused")
fun setupCrashlytics() {
    Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
}