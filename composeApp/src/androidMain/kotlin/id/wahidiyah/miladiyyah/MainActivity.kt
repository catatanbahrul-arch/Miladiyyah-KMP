package id.wahidiyah.miladiyyah

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            updateLocationAndRefresh()
        } else {
            Toast.makeText(this, "Izin GPS ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val updateAction = {
            val hasGpsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasGpsPermission) {
                updateLocationAndRefresh()
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }

        setContent { App(onUpdateLocation = updateAction) }
    }
    
    private fun updateLocationAndRefresh() {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            
            location?.let {
                id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.latitude = it.latitude
                id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.longitude = it.longitude
                id.wahidiyah.miladiyyah.alarm.AlarmScheduler.scheduleAll(this)
                Toast.makeText(this, "Lokasi Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Gagal mendapatkan titik GPS", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            Toast.makeText(this, "Harap aktifkan GPS di pengaturan HP", Toast.LENGTH_SHORT).show()
        }
    }
}
