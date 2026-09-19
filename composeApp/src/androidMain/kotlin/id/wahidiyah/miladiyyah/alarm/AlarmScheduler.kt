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
