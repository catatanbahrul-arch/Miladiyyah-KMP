package id.wahidiyah.miladiyyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Minta izin Notifikasi Pop-up dengan cara paling aman
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
            
            // Daftarkan alarm diam-diam di belakang layar tanpa mengganggu pembukaan aplikasi
            AlarmScheduler.scheduleAll(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try { id.wahidiyah.miladiyyah.alarm.AlarmScheduler.scheduleAll(this) } catch (e: Exception) {}
        setContent { App() }
    }
}
