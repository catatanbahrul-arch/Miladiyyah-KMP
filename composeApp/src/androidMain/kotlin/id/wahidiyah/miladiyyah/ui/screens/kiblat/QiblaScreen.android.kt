package id.wahidiyah.miladiyyah.ui.screens.kiblat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.wahidiyah.miladiyyah.theme.*
import kotlin.math.roundToInt

@Composable
actual fun QiblaScreen() {
    val context = LocalContext.current
    var azimuth by remember { mutableStateOf(0f) }
    var qiblaBearing by remember { mutableStateOf(294.0f) } 
    var hasGps by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                    val orientationValues = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    var degree = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    if (degree < 0) degree += 360f
                    azimuth = degree
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) { sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI) }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                
                location?.let {
                    val kaaba = Location("").apply { latitude = 21.422487; longitude = 39.826206 }
                    var bearing = it.bearingTo(kaaba)
                    if (bearing < 0) bearing += 360f
                    qiblaBearing = bearing
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.latitude = it.latitude
                    id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine.longitude = it.longitude
                    hasGps = true
                }
            } catch (e: Exception) { }
        }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Arah Kiblat", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (hasGps) "Akurat berdasarkan GPS Anda" else "Menggunakan titik kordinat default (Kediri)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = DeepForestGreen, style = Stroke(width = 8f))
                    drawCircle(color = Color.LightGray, style = Stroke(width = 2f), radius = size.width / 2 - 20f)
                }

                Box(modifier = Modifier.fillMaxSize().rotate(-azimuth), contentAlignment = Alignment.TopCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                        Text("U", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(4.dp).height(100.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                    }
                }

                Box(modifier = Modifier.fillMaxSize().rotate(qiblaBearing - azimuth), contentAlignment = Alignment.TopCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Kaaba", tint = SubtleGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(modifier = Modifier.width(8.dp).height(130.dp).background(SubtleGold, RoundedCornerShape(4.dp)))
                    }
                }

                Box(modifier = Modifier.size(24.dp).background(DeepForestGreen, CircleShape))
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Putar HP Anda hingga jarum emas sejajar lurus menghadap atas.", fontSize = 13.sp, color = DeepForestGreen, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sudut Kiblat: ${qiblaBearing.roundToInt()}°", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                }
            }
        }
    }
}
