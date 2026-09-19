package id.wahidiyah.miladiyyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // HANYA MEMUAT UI, TIDAK ADA LOGIKA BERAT LAINNYA. INI MENJAMIN APLIKASI ANTI-CRASH.
        setContent {
            App()
        }
    }
}
