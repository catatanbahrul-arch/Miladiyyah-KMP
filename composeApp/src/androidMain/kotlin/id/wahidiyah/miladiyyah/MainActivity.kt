package id.wahidiyah.miladiyyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import id.wahidiyah.miladiyyah.core.utils.AppCache

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inisialisasi AppCache Persistence SharedPreferences
        val prefs = getSharedPreferences("wahidiyah_cache", MODE_PRIVATE)
        AppCache.save = { key, value -> prefs.edit().putString(key, value).apply() }
        AppCache.load = { key -> prefs.getString(key, null) }

        // Daftarkan ulang alarm saat aplikasi dibuka jika diaktifkan
        try {
            AlarmScheduler.rescheduleAllEnabled(this)
        } catch (e: Exception) {
            // Abaikan jika permission belum diberikan
        }

        setContent {
            App(
                onUpdateLocation = {
                    // Trigger penjadwalan ulang saat lokasi diperbarui
                    try {
                        AlarmScheduler.rescheduleAllEnabled(this)
                    } catch (e: Exception) {}
                }
            )
        }
    }
}
