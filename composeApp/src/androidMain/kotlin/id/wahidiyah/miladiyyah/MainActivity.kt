package id.wahidiyah.miladiyyah

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val settingsClient by lazy {
        LocationServices.getSettingsClient(this)
    }

    private val locationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("wahidiyah_cache", MODE_PRIVATE)

        AppCache.save = { key, value ->
            prefs.edit().putString(key, value).apply()
        }

        AppCache.load = { key ->
            prefs.getString(key, null)
        }

        restoreCachedLocation()

        locationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->

                val fineGranted =
                    result[Manifest.permission.ACCESS_FINE_LOCATION] == true

                val coarseGranted =
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                if (fineGranted || coarseGranted) {
                    ensureLocationSettingsAndFetch()
                } else {
                    PrayerTimeEngine.locationName =
                        "Izin lokasi belum diberikan"
                }
            }

        locationSettingsLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    fetchCurrentLocation()
                } else {
                    PrayerTimeEngine.locationName =
                        cachedLocationLabel()
                            ?: "GPS belum diaktifkan"
                }
            }

        try {
            AlarmScheduler.rescheduleAllEnabled(this)
        } catch (_: Exception) {
        }

        setContent {
            App(
                onUpdateLocation = {
                    requestFreshLocation()
                }
            )
        }
    }

    private fun requestFreshLocation() {
        PrayerTimeEngine.locationName = "Mendeteksi lokasi..."

        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        ensureLocationSettingsAndFetch()
    }

    private fun hasLocationPermission(): Boolean {
        val fine =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun ensureLocationSettingsAndFetch() {
        if (!hasLocationPermission()) {
            PrayerTimeEngine.locationName =
                "Izin lokasi belum diberikan"
            return
        }

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10_000L
            )
                .setMinUpdateIntervalMillis(5_000L)
                .setWaitForAccurateLocation(false)
                .build()

        val settingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        settingsClient
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                fetchCurrentLocation()
            }
            .addOnFailureListener { error ->

                if (error is ResolvableApiException) {
                    try {
                        val intentRequest =
                            IntentSenderRequest.Builder(
                                error.resolution
                            ).build()

                        locationSettingsLauncher.launch(intentRequest)
                    } catch (_: Exception) {
                        fallbackToLastKnownLocation()
                    }
                } else {
                    fallbackToLastKnownLocation()
                }
            }
    }

    private fun fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            PrayerTimeEngine.locationName =
                "Izin lokasi belum diberikan"
            return
        }

        PrayerTimeEngine.locationName =
            "Mendeteksi lokasi..."

        val cancellationTokenSource =
            CancellationTokenSource()

        locationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location ->

                cancellationTokenSource.cancel()

                if (location != null) {
                    applyLocation(location, false)
                } else {
                    fallbackToLastKnownLocation()
                }
            }
            .addOnFailureListener {
                cancellationTokenSource.cancel()
                fallbackToLastKnownLocation()
            }
    }

    private fun fallbackToLastKnownLocation() {
        if (!hasLocationPermission()) {
            PrayerTimeEngine.locationName =
                "Izin lokasi belum diberikan"
            return
        }

        locationClient
            .lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {
                    applyLocation(location, true)
                } else {
                    PrayerTimeEngine.locationName =
                        cachedLocationLabel()
                            ?: "Lokasi belum tersedia"
                }
            }
            .addOnFailureListener {
                PrayerTimeEngine.locationName =
                    cachedLocationLabel()
                        ?: "Lokasi belum tersedia"
            }
    }

    private fun applyLocation(
        location: Location,
        usedLastKnown: Boolean
    ) {
        val latitude = location.latitude
        val longitude = location.longitude

        if (
            !latitude.isFinite() ||
            !longitude.isFinite() ||
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            PrayerTimeEngine.locationName =
                cachedLocationLabel()
                    ?: "Koordinat lokasi tidak valid"
            return
        }

        PrayerTimeEngine.updateLocation(
            latitude = latitude,
            longitude = longitude,
            name = if (usedLastKnown) {
                "Lokasi terakhir"
            } else {
                "Menyempurnakan lokasi..."
            }
        )

        AppCache.save(
            "LOCATION_LATITUDE",
            latitude.toString()
        )

        AppCache.save(
            "LOCATION_LONGITUDE",
            longitude.toString()
        )

        locationScope.launch {
            val name =
                withContext(Dispatchers.IO) {
                    resolveLocationName(
                        latitude,
                        longitude
                    )
                }

            val finalName =
                name ?: if (usedLastKnown) {
                    "Lokasi terakhir"
                } else {
                    "Lokasi terdeteksi"
                }

            PrayerTimeEngine.updateLocation(
                latitude = latitude,
                longitude = longitude,
                name = finalName
            )

            AppCache.save(
                "LOCATION_NAME",
                finalName
            )

            try {
                AlarmScheduler.rescheduleAllEnabled(
                    this@MainActivity
                )
            } catch (_: Exception) {
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLocationName(
        latitude: Double,
        longitude: Double
    ): String? {

        return try {
            if (!Geocoder.isPresent()) {
                return null
            }

            val addresses =
                Geocoder(this)
                    .getFromLocation(
                        latitude,
                        longitude,
                        1
                    )

            val address =
                addresses?.firstOrNull()
                    ?: return null

            val locality =
                address.locality?.trim().orEmpty()

            val subAdmin =
                address.subAdminArea?.trim().orEmpty()

            val admin =
                address.adminArea?.trim().orEmpty()

            when {
                locality.isNotBlank() &&
                    admin.isNotBlank() &&
                    !locality.equals(
                        admin,
                        ignoreCase = true
                    ) ->
                    "$locality, $admin"

                locality.isNotBlank() ->
                    locality

                subAdmin.isNotBlank() &&
                    admin.isNotBlank() ->
                    "$subAdmin, $admin"

                subAdmin.isNotBlank() ->
                    subAdmin

                admin.isNotBlank() ->
                    admin

                else ->
                    null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun restoreCachedLocation() {
        val lat =
            AppCache.load("LOCATION_LATITUDE")
                ?.toDoubleOrNull()

        val lng =
            AppCache.load("LOCATION_LONGITUDE")
                ?.toDoubleOrNull()

        val name =
            AppCache.load("LOCATION_NAME")

        if (
            lat != null &&
            lng != null &&
            lat.isFinite() &&
            lng.isFinite() &&
            lat in -90.0..90.0 &&
            lng in -180.0..180.0
        ) {
            PrayerTimeEngine.updateLocation(
                latitude = lat,
                longitude = lng,
                name = name?.takeIf { it.isNotBlank() }
                    ?: "Lokasi terakhir"
            )
        }
    }

    private fun cachedLocationLabel(): String? {
        return AppCache.load("LOCATION_NAME")
            ?.takeIf { it.isNotBlank() }
    }

    override fun onDestroy() {
        locationScope.cancel()
        super.onDestroy()
    }
}
