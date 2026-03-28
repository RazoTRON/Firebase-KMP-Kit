import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.firebasekit.BuildConfig
import java.awt.Dimension
import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.sample.App

fun main() {
    Firebase.initialize(
        apiKey = BuildConfig.FIREBASE_API_KEY,
        projectId = BuildConfig.FIREBASE_PROJECT_ID,
        appId = BuildConfig.FIREBASE_APP_ID,
    )

    application {
        Window(
            title = "Firebase Kit",
            state = rememberWindowState(width = 500.dp, height = 900.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            App()
        }
    }
}
