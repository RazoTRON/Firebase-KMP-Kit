import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.messaging.setFirebaseMessagingApnsToken
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData

fun Configure() {
    Firebase.initialize()
}

@OptIn(ExperimentalForeignApi::class)
fun ForwardApnsToken(deviceToken: NSData) {
    setFirebaseMessagingApnsToken(deviceToken)
}
