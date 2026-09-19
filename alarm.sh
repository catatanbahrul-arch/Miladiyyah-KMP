#!/bin/bash
set -e

echo "=========================================================="
echo " MEMULAI OPERASI PERBAIKAN SISTEM ALARM & NOTIFIKASI WAHIDIYAH"
echo "=========================================================="

echo "==> [1/6] MEMBANGUN ALARM CONTROLLER TERPUSAT (ALARM SCHEDULER V2)..."
mkdir -p composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/alarm
cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/alarm/AlarmScheduler.kt
package id.wahidiyah.miladiyyah.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    // ID Unik untuk memastikan PendingIntent konsisten saat pembuatan & pembatalan
    private const val ID_SUBUH = 101
    private const val ID_DZUHUR = 102
    private const val ID_ASHAR = 103
    private const val ID_MAGHRIB = 104
    private const val ID_ISYA = 105
    private const val ID_TARHIM = 201
    private const val ID_TASYAFUAN = 301
    private const val ID_DANABOX_MORNING = 401
    private const val ID_DANABOX_EVENING = 402

    fun rescheduleAllEnabled(context: Context) {
        Log.d(TAG, "Mengeksekusi ulang seluruh jadwal alarm...")
        val isAdzanOn = AppCache.loadBoolean("ALARM_ADZAN", true)
        val isTarhimOn = AppCache.loadBoolean("ALARM_TARHIM", true)
        val isTasyafuanOn = AppCache.loadBoolean("ALARM_TASYAFUAN", true)
        val isDanaBoxOn = AppCache.loadBoolean("ALARM_DANABOX", true)

        if (isAdzanOn) scheduleAdzan(context) else cancelAdzan(context)
        if (isTarhimOn) scheduleTarhim(context) else cancelTarhim(context)
        if (isTasyafuanOn) scheduleTasyafuan(context) else cancelTasyafuan(context)
        if (isDanaBoxOn) scheduleDanaBox(context) else cancelDanaBox(context)
    }

    private fun setAlarm(context: Context, id: Int, actionStr: String, title: String, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = actionStr
            putExtra("ALARM_TITLE", title)
            putExtra("ALARM_ID", id)
        }
        
        // FLAG_UPDATE_CURRENT WAJIB agar intent tidak tumpang tindih
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Verifikasi Permission Alarm Presisi (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e(TAG, "Gagal set alarm $title: Permission EXACT_ALARM ditolak OS.")
            return
        }

        try {
            // Menerobos mode Doze dengan aman
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            Log.d(TAG, "BERHASIL: Alarm $title dijadwalkan pada timestamp $timeInMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security Exception saat mengatur Alarm: ${e.message}")
        }
    }

    private fun cancelSpecificAlarm(context: Context, id: Int, actionStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply { action = actionStr }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "DIBATALKAN: Alarm ID $id telah dicabut.")
    }

    private fun scheduleAdzan(context: Context) {
        try {
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(tz)
            val prayers = PrayerTimeEngine.getPrayers(now.date)

            prayers.forEach { prayer ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, prayer.time.hour)
                    set(Calendar.MINUTE, prayer.time.minute)
                    set(Calendar.SECOND, 0)
                }
                // Jika waktu terlewat, lewati hari ini (Sistem akan refresh harian secara otomatis)
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    val (id, action) = when (prayer.type.name) {
                        "SUBUH" -> Pair(ID_SUBUH, "ACTION_ADZAN_SUBUH")
                        "DZUHUR" -> Pair(ID_DZUHUR, "ACTION_ADZAN_DZUHUR")
                        "ASHAR" -> Pair(ID_ASHAR, "ACTION_ADZAN_ASHAR")
                        "MAGHRIB" -> Pair(ID_MAGHRIB, "ACTION_ADZAN_MAGHRIB")
                        "ISYA" -> Pair(ID_ISYA, "ACTION_ADZAN_ISYA")
                        else -> Pair(0, "")
                    }
                    if (id != 0) setAlarm(context, id, action, "Adzan ${prayer.type.title}", calendar.timeInMillis)
                }
            }
        } catch (e: Exception) { Log.e(TAG, "Gagal sinkronisasi mesin Adzan: ${e.message}") }
    }

    private fun cancelAdzan(context: Context) {
        cancelSpecificAlarm(context, ID_SUBUH, "ACTION_ADZAN_SUBUH")
        cancelSpecificAlarm(context, ID_DZUHUR, "ACTION_ADZAN_DZUHUR")
        cancelSpecificAlarm(context, ID_ASHAR, "ACTION_ADZAN_ASHAR")
        cancelSpecificAlarm(context, ID_MAGHRIB, "ACTION_ADZAN_MAGHRIB")
        cancelSpecificAlarm(context, ID_ISYA, "ACTION_ADZAN_ISYA")
    }

    private fun scheduleTarhim(context: Context) {
        try {
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(tz)
            val prayers = PrayerTimeEngine.getPrayers(now.date)
            val subuh = prayers.find { it.type.name == "SUBUH" }
            if (subuh != null) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, subuh.time.hour)
                    set(Calendar.MINUTE, subuh.time.minute)
                    set(Calendar.SECOND, 0)
                    add(Calendar.MINUTE, -15) // Mundur 15 Menit dari Subuh
                }
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    setAlarm(context, ID_TARHIM, "ACTION_TARHIM", "Persiapan Subuh (Tarhim)", calendar.timeInMillis)
                }
            }
        } catch (e: Exception) {}
    }

    private fun cancelTarhim(context: Context) { cancelSpecificAlarm(context, ID_TARHIM, "ACTION_TARHIM") }

    private fun scheduleTasyafuan(context: Context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        setAlarm(context, ID_TASYAFUAN, "ACTION_TASYAFUAN", "Pengingat Tasyafu'an", calendar.timeInMillis)
    }

    private fun cancelTasyafuan(context: Context) { cancelSpecificAlarm(context, ID_TASYAFUAN, "ACTION_TASYAFUAN") }

    private fun scheduleDanaBox(context: Context) {
        val calPagi = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        if (calPagi.timeInMillis <= System.currentTimeMillis()) calPagi.add(Calendar.DAY_OF_YEAR, 1)
        setAlarm(context, ID_DANABOX_MORNING, "ACTION_DANABOX", "Dana Box Pagi", calPagi.timeInMillis)

        val calMalam = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        if (calMalam.timeInMillis <= System.currentTimeMillis()) calMalam.add(Calendar.DAY_OF_YEAR, 1)
        setAlarm(context, ID_DANABOX_EVENING, "ACTION_DANABOX", "Dana Box Malam", calMalam.timeInMillis)
    }

    private fun cancelDanaBox(context: Context) {
        cancelSpecificAlarm(context, ID_DANABOX_MORNING, "ACTION_DANABOX")
        cancelSpecificAlarm(context, ID_DANABOX_EVENING, "ACTION_DANABOX")
    }
}
EOF

echo "==> [2/6] MENGATASI ISU NOTIFIKASI DAN AUDIO SERVICE..."
cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/alarm/AlarmReceiver.kt
package id.wahidiyah.miladiyyah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val title = intent.getStringExtra("ALARM_TITLE") ?: "Waktu Tiba"
        val alarmId = intent.getIntExtra("ALARM_ID", 0)

        // Setelah alarm meledak, jadwalkan ulang untuk hari berikutnya.
        AlarmScheduler.rescheduleAllEnabled(context)

        val serviceIntent = Intent(context, AudioService::class.java).apply {
            this.action = action
            putExtra("ALARM_TITLE", title)
            putExtra("ALARM_ID", alarmId)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Log fallback jika OS memblokir Background Service
        }
    }
}
EOF

cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/alarm/AudioService.kt
package id.wahidiyah.miladiyyah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.wahidiyah.miladiyyah.MainActivity

class AudioService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "WAHIDIYAH_ALARM_CHANNEL"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val title = intent?.getStringExtra("ALARM_TITLE") ?: "Pemberitahuan Wahidiyah"
        val alarmId = intent?.getIntExtra("ALARM_ID", 1001) ?: 1001

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Menggunakan ikon aplikasi (Akar masalah nomor 1 diselesaikan di sini)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wahidiyah")
            .setContentText(title)
            .setSmallIcon(resources.getIdentifier("ic_launcher_foreground", "drawable", packageName).takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        startForeground(alarmId, notification)

        // Penanganan Mencegah Multiple Audio Bentrok (Duplikat)
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        val audioResource = when {
            action?.contains("ADZAN") == true -> "adzan"
            action == "ACTION_TARHIM" -> "tarhim"
            action == "ACTION_TASYAFUAN" -> "tasyafuan"
            action == "ACTION_DANABOX" -> "danabox"
            else -> null
        }

        if (audioResource != null) {
            val resId = resources.getIdentifier(audioResource, "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId).apply {
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                        stopForeground(true)
                        stopSelf() // WAJIB: Mematikan Service agar baterai aman
                    }
                    start()
                }
            } else {
                stopForeground(true)
                stopSelf()
            }
        } else {
            stopForeground(true)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Alarm Wahidiyah", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Pemberitahuan Waktu Salat dan Pengingat" }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
EOF

echo "==> [3/6] MEMPERBAIKI KETAHANAN TERHADAP HP REBOOT (BOOT RECEIVER)..."
cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/alarm/BootReceiver.kt
package id.wahidiyah.miladiyyah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            // Mengembalikan jadwal alarm HANYA untuk fitur yang diset ON oleh User.
            AlarmScheduler.rescheduleAllEnabled(context)
        }
    }
}
EOF

echo "==> [4/6] MEMASANG PERMISSION RECEIVER DI MANIFEST..."
sed -i '/<application/i \    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"\/>\n    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"\/>\n    <uses-permission android:name="android.permission.USE_EXACT_ALARM"\/>\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"\/>\n    <uses-permission android:name="android.permission.WAKE_LOCK"\/>' composeApp/src/androidMain/AndroidManifest.xml
sed -i '/<\/application>/i \        <receiver android:name=".alarm.BootReceiver" android:exported="true">\n            <intent-filter>\n                <action android:name="android.intent.action.BOOT_COMPLETED"\/>\n                <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED"\/>\n            <\/intent-filter>\n        <\/receiver>\n\n        <receiver android:name=".alarm.AlarmReceiver" android:exported="false" \/>\n\n        <service android:name=".alarm.AudioService" android:exported="false" \/>' composeApp/src/androidMain/AndroidManifest.xml

echo "==> [5/6] MENGHUBUNGKAN UI SWITCH (SALAT SCREEN) LANGSUNG KE ALARM SCHEDULER..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/salat/SalatScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.datetime.*

// Bridge untuk memanggil Native Android Scheduler
expect fun updateAlarmSchedules()

@Composable
fun SalatScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    val prayers = try { PrayerTimeEngine.getPrayers(Clock.System.now().toLocalDateTime(tz).date) } catch(e:Exception) { emptyList() }

    var adzanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true)) }
    var tarhimEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true)) }
    var tasyafuanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true)) }
    var danaBoxEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true)) }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column {
                Text("Waktu Salat", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier=Modifier.height(4.dp))
                Text(PrayerTimeEngine.locationName, color = BrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            Text("JADWAL HARI INI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    prayers.forEachIndexed { index, prayer ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val isSilent = prayer.type == PrayerType.IMSAK || prayer.type == PrayerType.TERBIT || prayer.type == PrayerType.DHUHA
                            Box(modifier = Modifier.size(8.dp).background(if(isSilent) Border else BrandAccent, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(prayer.type.title, fontSize = 16.sp, color = if(isSilent) TextSecondary else TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text("${prayer.time.hour.toString().padStart(2,'0')}:${prayer.time.minute.toString().padStart(2,'0')}", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (index < prayers.size - 1) { HorizontalDivider(color = Background, thickness = 2.dp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandPrimary), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().clickable { onNavigateToKiblat() }) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Surface, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Buka Kompas Kiblat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Surface)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text("PENGATURAN PENGINGAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Peringatan masuk waktu", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it; AppCache.saveBoolean("ALARM_ADZAN", it); updateAlarmSchedules() }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum waktu Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it; AppCache.saveBoolean("ALARM_TARHIM", it); updateAlarmSchedules() }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Tasyafu'an", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Setiap 03:00 WIB", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tasyafuanEnabled, onCheckedChange = { tasyafuanEnabled = it; AppCache.saveBoolean("ALARM_TASYAFUAN", it); updateAlarmSchedules() }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Dana Box", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Pukul 06:00 & 19:00", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it; AppCache.saveBoolean("ALARM_DANABOX", it); updateAlarmSchedules() }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
EOF

cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/salat/SalatScreen.android.kt
package id.wahidiyah.miladiyyah.ui.screens.salat
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import id.wahidiyah.miladiyyah.MainActivity

actual fun updateAlarmSchedules() {
    MainActivity.instance?.let { context ->
        AlarmScheduler.rescheduleAllEnabled(context)
    }
}
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/salat/SalatScreen.ios.kt
package id.wahidiyah.miladiyyah.ui.screens.salat
actual fun updateAlarmSchedules() {
    // Implementasi iOS (Local Notifications) jika kelak dikembangkan
}
EOF

echo "==> [6/6] MENGIRIM PERBAIKAN KE GITHUB..."
git add .
git commit -m "fix: master alarm controller resolution. Switch now directly schedules/cancels exact alarms based on persistent states. Introduced BootReceiver for safe reboot resurrection. Implemented Foreground Service with safe notification icon matching Brand Identity."
git push origin main
echo "=========================================================="
echo " EKSEKUSI PERBAIKAN ALARM BERHASIL DI-PUSH!"
echo "=========================================================="
EOF
