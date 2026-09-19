package id.wahidiyah.miladiyyah

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    // Launcher canggih yang kebal dari crash
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        updateLocationIfGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load UI duluan biar cepat dan ga crash!
        setContent { App() }

        // Minta Izin & GPS di belakang layar setelah UI tampil
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                
                val missing = perms.filter { ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED }
                if (missing.isNotEmpty()) {
                    launch(Dispatchers.Main) {
                        permissionLauncher.launch(missing.toTypedArray())
                    }
                } else {
                    updateLocationIfGranted()
                }
            } catch(e: Exception) {}
        }
    }
    
    private fun updateLocationIfGranted() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                
                location?.let {
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.latitude = it.latitude
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.longitude = it.longitude
                    
                    // Jadwalkan ulang alarm Adzan sesuai lokasi baru!
                    AlarmScheduler.scheduleAll(this)
                }
            }
        } catch(e: Exception) {}
    }
}
