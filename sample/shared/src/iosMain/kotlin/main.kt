import androidx.compose.ui.window.ComposeUIViewController
import com.firebasekit.sample.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { 
    App()
}