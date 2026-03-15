import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.firebasekit.sample.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
//    Firebase.initialize(
//        apiKey = FIREBASE_API_KEY,
//        authDomain = FIREBASE_AUTH_DOMAIN,
//        projectId = FIREBASE_PROJECT_ID,
//        storageBucket = FIREBASE_STORAGE_BUCKET,
//        messagingSenderId = FIREBASE_MESSAGING_SENDER_ID,
//        appId = FIREBASE_APP_ID,
//        measurementId = FIREBASE_MEASUREMENT_ID,
//    )
//    Firebase.remoteConfig.getLong("lifetime_success_matches_to_show_time_offer")?.let {
//        println("SSSSS $it")
//    }
    ComposeViewport { App() }
}