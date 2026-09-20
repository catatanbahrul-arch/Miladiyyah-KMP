package id.wahidiyah.miladiyyah

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlinx.datetime.Clock

class MainActivity : ComponentActivity() {

    private val locationClient by lazy {
        LocationServices
            .getFusedLocationProviderClient(this)
    }

    private val settingsClient by lazy {
        LocationServices
            .getSettingsClient(this)
    }

    private val locationScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Main.immediate
        )

    private lateinit var locationPermissionLauncher:
        ActivityResultLauncher<Array<String>>

    private lateinit var locationSettingsLauncher:
        ActivityResultLauncher<IntentSenderRequest>

    private lateinit var notificationPermissionLauncher:
        ActivityResultLauncher<String>

    private lateinit var exactAlarmSettingsLauncher:
        ActivityResultLauncher<Intent>

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val prefs =
            getSharedPreferences(
                "wahidiyah_cache",
                MODE_PRIVATE
            )

        AlarmScheduler.initialize(this)
        AlarmScheduler.prepareNotificationChannel(this)

        AppCache.save = { key, value ->
            prefs.edit()
                .putString(key, value)
                .apply()
        }

        AppCache.load = { key ->
            prefs.getString(
                key,
                null
            )
        }

        PrayerTimeEngine.clearLocation()

        // Hapus semua alarm yang mungkin masih berasal
        // dari versi lama. Alarm baru hanya dibuat setelah
        // lokasi + notifikasi + exact alarm siap.
        AlarmScheduler.cancelAllKnownAlarms(this)
        markLocationNotReady()

        locationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .RequestMultiplePermissions()
            ) { result ->
                val fineGranted =
                    result[
                        Manifest.permission
                            .ACCESS_FINE_LOCATION
                    ] == true

                val coarseGranted =
                    result[
                        Manifest.permission
                            .ACCESS_COARSE_LOCATION
                    ] == true

                if (fineGranted) {
                    continueStartupProvisioning()
                } else if (coarseGranted) {
                    Toast.makeText(
                        this,
                        "Pilih lokasi PRESISI agar jadwal shalat akurat.",
                        Toast.LENGTH_LONG
                    ).show()

                    openAppLocationSettings()
                } else {
                    Toast.makeText(
                        this,
                        "Izin lokasi diperlukan untuk jadwal shalat dan Adzan.",
                        Toast.LENGTH_LONG
                    ).show()

                    PrayerTimeEngine.locationName =
                        "Izin lokasi diperlukan"
                }
            }

        locationSettingsLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .StartIntentSenderForResult()
            ) { result ->
                if (
                    result.resultCode ==
                        RESULT_OK
                ) {
                    refreshCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Aktifkan layanan lokasi/GPS terlebih dahulu.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .RequestPermission()
            ) { granted ->
                if (granted) {
                    continueAfterNotificationPermission()
                } else {
                    Toast.makeText(
                        this,
                        "Izin notifikasi diperlukan untuk Adzan dan pengingat.",
                        Toast.LENGTH_LONG
                    ).show()

                    openAppNotificationSettings()
                }
            }

        exactAlarmSettingsLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .StartActivityForResult()
            ) {
                continueAfterExactAlarmSettings()
            }

        setContent {
            App(
                onUpdateLocation = {
                    startStartupProvisioning()
                }
            )
        }

        locationScope.launch {
            delay(350L)
            startStartupProvisioning()
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            ::exactAlarmSettingsLauncher.isInitialized &&
            PrayerTimeEngine.locationReady &&
            hasPreciseLocationPermission() &&
            hasNotificationPermission() &&
            hasExactAlarmPermission()
        ) {
            AlarmScheduler.rescheduleAllEnabled(this)
        }
    }

    private fun startStartupProvisioning() {
        PrayerTimeEngine.clearLocation()
        AlarmScheduler.cancelAllKnownAlarms(this)
        markLocationNotReady()

        PrayerTimeEngine.locationName =
            "Menyiapkan lokasi GPS..."

        continueStartupProvisioning()
    }

    private fun continueStartupProvisioning() {
        if (!hasPreciseLocationPermission()) {
            requestPreciseLocation()
            return
        }

        ensureLocationServices()
    }

    private fun requestPreciseLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission
                    .ACCESS_FINE_LOCATION,
                Manifest.permission
                    .ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun hasPreciseLocationPermission():
        Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureLocationServices() {
        if (!hasPreciseLocationPermission()) {
            return
        }

        val request =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10_000L
            )
                .setMinUpdateIntervalMillis(
                    5_000L
                )
                .setWaitForAccurateLocation(true)
                .build()

        val settingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()

        settingsClient
            .checkLocationSettings(
                settingsRequest
            )
            .addOnSuccessListener {
                refreshCurrentLocation()
            }
            .addOnFailureListener { error ->
                if (
                    error is ResolvableApiException
                ) {
                    try {
                        locationSettingsLauncher
                            .launch(
                                IntentSenderRequest
                                    .Builder(
                                        error.resolution
                                    )
                                    .build()
                            )
                    } catch (_: Exception) {
                        Toast.makeText(
                            this,
                            "Tidak dapat membuka pengaturan GPS.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "GPS belum siap.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun refreshCurrentLocation() {
        if (!hasPreciseLocationPermission()) {
            return
        }

        PrayerTimeEngine.locationName =
            "Mencari lokasi GPS terbaru..."

        locationScope.launch {
            val current =
                try {
                    obtainFreshPreciseLocation()
                } catch (_: Exception) {
                    null
                }

            val source: String
            val usableLocation: Location?

            if (
                current != null &&
                isUsableCurrentLocation(current)
            ) {
                source = "FUSED_CURRENT"
                usableLocation = current
            } else {
                source = "FUSED_LAST_KNOWN"
                usableLocation =
                    obtainUsableLastKnownLocation()
            }

            if (usableLocation == null) {
                markLocationNotReady()

                PrayerTimeEngine.clearLocation()
                PrayerTimeEngine.locationName =
                    "GPS belum cukup presisi"

                Toast.makeText(
                    this@MainActivity,
                    "Lokasi GPS belum cukup presisi. Pindah ke area terbuka lalu tekan Update.",
                    Toast.LENGTH_LONG
                ).show()

                return@launch
            }

            persistLocation(
                usableLocation,
                source
            )

            // HANYA setelah lokasi benar-benar tersimpan,
            // lanjut ke izin notifikasi lalu exact alarm.
            ensureNotificationPermission()
        }
    }

    private suspend fun obtainFreshPreciseLocation():
        Location? {
        for (attempt in 1..3) {
            val location =
                awaitCurrentLocation(
                    timeoutMillis = 12_000L
                )

            if (
                location != null &&
                isUsableCurrentLocation(location)
            ) {
                return location
            }

            delay(700L)
        }

        return null
    }

    private suspend fun awaitCurrentLocation(
        timeoutMillis: Long
    ): Location? =
        withTimeoutOrNull(
            timeoutMillis
        ) {
            suspendCancellableCoroutine { cont ->
                val tokenSource =
                    CancellationTokenSource()

                locationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        tokenSource.token
                    )
                    .addOnSuccessListener {
                        location ->
                        if (cont.isActive) {
                            cont.resume(location)
                        }
                    }
                    .addOnFailureListener {
                        error ->
                        if (cont.isActive) {
                            cont.resumeWith(
                                Result.failure(
                                    error
                                )
                            )
                        }
                    }

                cont.invokeOnCancellation {
                    tokenSource.cancel()
                }
            }
        }

    private suspend fun obtainUsableLastKnownLocation():
        Location? {
        return try {
            suspendCancellableCoroutine { cont ->
                locationClient
                    .lastLocation
                    .addOnSuccessListener {
                        location ->
                        if (
                            cont.isActive &&
                            location != null &&
                            isUsableLastKnownLocation(
                                location
                            )
                        ) {
                            cont.resume(location)
                        } else if (
                            cont.isActive
                        ) {
                            cont.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        if (cont.isActive) {
                            cont.resume(null)
                        }
                    }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isUsableCurrentLocation(
        location: Location
    ): Boolean {
        if (
            !location.latitude.isFinite() ||
            !location.longitude.isFinite()
        ) {
            return false
        }

        if (
            location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0
        ) {
            return false
        }

        if (!location.hasAccuracy()) {
            return false
        }

        if (
            location.accuracy.toDouble() >
                250.0
        ) {
            return false
        }

        val age =
            (
                System.currentTimeMillis() -
                    location.time
                ).coerceAtLeast(0L)

        return (
            location.time > 0L &&
                age <=
                    2L * 60L * 1000L
        )
    }

    private fun isUsableLastKnownLocation(
        location: Location
    ): Boolean {
        if (
            !location.latitude.isFinite() ||
            !location.longitude.isFinite()
        ) {
            return false
        }

        if (
            location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0
        ) {
            return false
        }

        if (!location.hasAccuracy()) {
            return false
        }

        if (
            location.accuracy.toDouble() >
                250.0
        ) {
            return false
        }

        val age =
            (
                System.currentTimeMillis() -
                    location.time
                ).coerceAtLeast(0L)

        return (
            location.time > 0L &&
                age <=
                    10L * 60L * 1000L
        )
    }

    private suspend fun persistLocation(
        location: Location,
        source: String
    ) {
        val accuracy =
            location.accuracy.toDouble()

        val elevation =
            if (
                location.hasAltitude() &&
                location.altitude.isFinite() &&
                location.altitude >= 0.0
            ) {
                location.altitude
            } else {
                0.0
            }

        val latitude =
            location.latitude

        val longitude =
            location.longitude

        val updatedAt =
            System.currentTimeMillis()

        AppCache.save(
            "LOCATION_READY",
            "false"
        )

        AppCache.save(
            "LOCATION_PRECISE",
            "true"
        )

        AppCache.save(
            "LOCATION_ACCURACY_METERS",
            accuracy.toString()
        )

        AppCache.save(
            "LOCATION_UPDATED_AT",
            updatedAt.toString()
        )

        AppCache.save(
            "LOCATION_SOURCE",
            source
        )

        AppCache.save(
            "LOCATION_ELEVATION",
            elevation.toString()
        )

        AppCache.save(
            "LOCATION_LATITUDE",
            latitude.toString()
        )

        AppCache.save(
            "LOCATION_LONGITUDE",
            longitude.toString()
        )

        val resolvedName =
            withContext(Dispatchers.IO) {
                resolveDetailedLocationName(
                    latitude,
                    longitude
                )
            }

        val finalName =
            resolvedName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Lokasi GPS"

        PrayerTimeEngine.updateLocation(
            latitude = latitude,
            longitude = longitude,
            name = finalName,
            elevationMeters = elevation,
            accuracyMeters = accuracy,
            updatedAtEpochMillis = updatedAt,
            source = source
        )

        AppCache.save(
            "LOCATION_NAME",
            finalName
        )

        AppCache.save(
            "LOCATION_READY",
            "true"
        )
    }

    @Suppress("DEPRECATION")
    private fun resolveDetailedLocationName(
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

            val parts =
                linkedSetOf<String>()

            fun addPart(
                value: String?
            ) {
                val clean =
                    value
                        ?.trim()
                        .orEmpty()

                if (
                    clean.isNotBlank() &&
                    !parts.any {
                        it.equals(
                            clean,
                            ignoreCase = true
                        )
                    }
                ) {
                    parts.add(clean)
                }
            }

            // Coba tampilkan area yang paling spesifik
            // yang diberikan provider Android.
            addPart(address.subLocality)
            addPart(address.locality)
            addPart(address.subAdminArea)
            addPart(address.adminArea)

            if (parts.isEmpty()) {
                return address
                    .getAddressLine(0)
                    ?.trim()
            }

            parts
                .take(4)
                .joinToString(", ")
                .ifBlank {
                    address
                        .getAddressLine(0)
                        ?.trim()
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureNotificationPermission() {
        if (
            AlarmScheduler
                .areNotificationsReady(
                    this
                )
        ) {
            continueAfterNotificationPermission()
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher
                .launch(
                    Manifest.permission
                        .POST_NOTIFICATIONS
                )
        } else {
            openAppNotificationSettings()
        }
    }

    private fun hasNotificationPermission():
        Boolean {
        return AlarmScheduler
            .areNotificationsReady(this)
    }

    private fun continueAfterNotificationPermission() {
        if (!hasNotificationPermission()) {
            return
        }

        ensureExactAlarmPermission()
    }

    private fun ensureExactAlarmPermission() {
        if (
            Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S
        ) {
            finalizeStartupProvisioning()
            return
        }

        if (hasExactAlarmPermission()) {
            finalizeStartupProvisioning()
            return
        }

        try {
            val intent =
                Intent(
                    Settings
                        .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {
                    data =
                        Uri.parse(
                            "package:$packageName"
                        )
                }

            exactAlarmSettingsLauncher
                .launch(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Aktifkan izin alarm presisi di pengaturan aplikasi.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun continueAfterExactAlarmSettings() {
        if (
            hasExactAlarmPermission()
        ) {
            finalizeStartupProvisioning()
        } else {
            Toast.makeText(
                this,
                "Izin alarm presisi belum aktif. Adzan belum dijadwalkan.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hasExactAlarmPermission():
        Boolean {
        if (
            Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S
        ) {
            return true
        }

        val alarmManager =
            getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        return alarmManager
            .canScheduleExactAlarms()
    }

    private fun finalizeStartupProvisioning() {
        if (!PrayerTimeEngine.locationReady) {
            return
        }

        markLocationReady()

        AlarmScheduler.rescheduleAllEnabled(
            this
        )
    }

    private fun markLocationNotReady() {
        AppCache.save(
            "LOCATION_READY",
            "false"
        )
        AppCache.save(
            "LOCATION_PRECISE",
            "false"
        )
    }

    private fun markLocationReady() {
        AppCache.save(
            "LOCATION_READY",
            "true"
        )
        AppCache.save(
            "LOCATION_PRECISE",
            "true"
        )
    }

    private fun openAppLocationSettings() {
        try {
            startActivity(
                Intent(
                    Settings
                        .ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {
                    data =
                        Uri.parse(
                            "package:$packageName"
                        )
                }
            )
        } catch (_: Exception) {
        }
    }

    private fun openAppNotificationSettings() {
        try {
            startActivity(
                Intent(
                    Settings
                        .ACTION_APP_NOTIFICATION_SETTINGS
                ).apply {
                    putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        packageName
                    )
                }
            )
        } catch (_: Exception) {
            openAppDetailsSettings()
        }
    }

    private fun openAppDetailsSettings() {
        try {
            startActivity(
                Intent(
                    Settings
                        .ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {
                    data =
                        Uri.parse(
                            "package:$packageName"
                        )
                }
            )
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        locationScope.cancel()
        super.onDestroy()
    }
}
