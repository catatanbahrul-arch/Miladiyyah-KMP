package id.wahidiyah.miladiyyah

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Minta Izin GPS & Notifikasi
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            
            val missingPermissions = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 101)
            }
            
            // Izin Alarm Tepat Waktu (Anti-Crash)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Ambil GPS secara diam-diam
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                location?.let {
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.latitude = it.latitude
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.longitude = it.longitude
                }
            }

            AlarmScheduler.scheduleAll(this)
        } catch (e: Exception) {
            e.printStackTrace() // Jika ada masalah sistem, HP tidak akan menutup aplikasi
        }

        setContent { App() }
    }
}
