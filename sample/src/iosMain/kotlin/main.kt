import androidx.compose.ui.window.ComposeUIViewController
import com.sebastianneubauer.jsontreesample.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
