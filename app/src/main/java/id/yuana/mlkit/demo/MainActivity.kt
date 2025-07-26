package id.yuana.mlkit.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import id.yuana.mlkit.demo.ui.components.CameraComponent
import id.yuana.mlkit.demo.ui.theme.MLKitDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MLKitDemoTheme {
                Surface {
                    CameraComponent()
                }
            }
        }
    }
}