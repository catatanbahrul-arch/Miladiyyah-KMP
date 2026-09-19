package id.wahidiyah.miladiyyah

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            updateLocationAndRefresh(true)
        } else {
            Toast.makeText(this, "Izin GPS ditolak", Toast.LENGTH_SHORT).show()
            id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.locationName = "Lokasi Default (Kediri)"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val updateAction = {
            val hasGps = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasGps) {
                updateLocationAndRefresh(true)
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }

        setContent { App(onUpdateLocation = updateAction) }

        // Minta lokasi diam-diam saat pertama buka
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    launch(Dispatchers.Main) { permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) }
                }
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    launch(Dispatchers.Main) { updateLocationAndRefresh(false) }
                } else {
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.locationName = "Lokasi Default (Kediri)"
                }
            } catch(e: Exception) {}
        }
    }
    
    private fun updateLocationAndRefresh(showToast: Boolean) {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            
            location?.let { loc ->
                id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.latitude = loc.latitude
                id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.longitude = loc.longitude
                
                // Menerjemahkan Koordinat ke Nama Kota
                val geocoder = Geocoder(this, Locale.getDefault())
                val onAddressFound = { address: android.location.Address? ->
                    address?.let {
                        val city = it.subAdminArea?.replace("Kabupaten ", "")?.replace("Kota ", "") ?: it.locality ?: "Tidak diketahui"
                        val sub = it.locality ?: it.subLocality ?: ""
                        id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.locationName = if(sub.isNotEmpty() && sub != city) "$sub, $city" else city
                    }
                }

                if (Build.VERSION.SDK_INT >= 33) {
                    geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) onAddressFound(addresses[0])
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (!addresses.isNullOrEmpty()) onAddressFound(addresses[0])
                }

                AlarmScheduler.scheduleAll(this)
                if (showToast) Toast.makeText(this, "Lokasi Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
            } ?: run {
                if (showToast) Toast.makeText(this, "Gagal mendapatkan titik GPS", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            if (showToast) Toast.makeText(this, "Harap aktifkan GPS di pengaturan HP", Toast.LENGTH_SHORT).show()
        }
    }
}
