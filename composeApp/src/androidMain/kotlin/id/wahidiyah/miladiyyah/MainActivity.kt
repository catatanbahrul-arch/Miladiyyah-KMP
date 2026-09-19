package id.wahidiyah.miladiyyah

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Meminta izin memunculkan Notifikasi Pop-up (Khusus Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        // COLOKKAN KABEL ALARM: Otomatis daftarkan semua jadwal notif & MP3 ke HP (AUTO-ON)
        AlarmScheduler.scheduleAll(this)

        setContent {
            App()
        }
    }
}
