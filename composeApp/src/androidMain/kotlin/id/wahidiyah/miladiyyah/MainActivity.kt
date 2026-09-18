package id.wahidiyah.miladiyyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Memanggil shared UI dari commonMain (App.kt)
        setContent {
            App()
        }
    }
}
