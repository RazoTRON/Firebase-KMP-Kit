import com.firebasekit.core.Firebase
import com.firebasekit.core.initialize
import com.firebasekit.remoteconfig.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Configure() {
    Firebase.initialize()
}