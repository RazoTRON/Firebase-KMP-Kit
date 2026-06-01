import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.firebasekit.BuildConfig
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.messaging.bridge.requestNotificationPermissions
import com.firebasekit.messaging.messaging
import com.firebasekit.sample.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Firebase.initialize(
        apiKey = BuildConfig.FIREBASE_API_KEY,
        authDomain = BuildConfig.FIREBASE_AUTH_DOMAIN,
        projectId = BuildConfig.FIREBASE_PROJECT_ID,
        storageBucket = BuildConfig.FIREBASE_STORAGE_BUCKET,
        messagingSenderId = BuildConfig.FIREBASE_MESSAGING_SENDER_ID,
        appId = BuildConfig.FIREBASE_APP_ID,
        measurementId = BuildConfig.FIREBASE_MEASUREMENT_ID,
    )

    CoroutineScope(Dispatchers.Default).launch {
        val granted = requestNotificationPermissions()

        if (granted) {
            Firebase.messaging.onMessage {
                println(it.notification?.title)
            }
        }
    }

    ComposeViewport { App() }
}