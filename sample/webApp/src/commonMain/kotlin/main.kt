import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.firebasekit.BuildConfig
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.sample.App

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

    ComposeViewport { App() }
}