package id.wahidiyah.miladiyyah.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine

object AlarmScheduler {
    fun scheduleAll(context: Context) {
        // 1. Syafa'an (03:00)
        scheduleAudio(context, "tasyafuan", 3, 0, 101)
        // 2. Dana Box Pagi (06:00)
        scheduleAudio(context, "danabox", 6, 0, 102)
        // 3. Dana Box Malam (19:00)
        scheduleAudio(context, "danabox", 19, 0, 103)
        
        // 4. Jadwal Salat, Tarhim, & Pop-up (-10 Menit)
        val prayers = PrayerTimeEngine.getTodayPrayers()
        prayers.forEachIndexed { index, prayer ->
            if (prayer.type.name == "TARHIM") {
                // Tarhim persis pada jamnya
                scheduleAudio(context, "tarhim", prayer.time.hour, prayer.time.minute, 200 + index)
            } else {
                // Adzan persis pada jam salat
                scheduleAudio(context, "adzan_${prayer.type.title}", prayer.time.hour, prayer.time.minute, 300 + index)
                
                // Pop-up Notifikasi Senyap 10 Menit Sebelum Waktu Salat (offset -10 menit)
                scheduleAudio(context, "popup_${prayer.type.title}", prayer.time.hour, prayer.time.minute, 400 + index, -10)
            }
        }
    }

    private fun scheduleAudio(context: Context, audioType: String, hour: Int, minute: Int, requestCode: Int, offsetMinutes: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("AUDIO_TYPE", audioType)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (offsetMinutes != 0) {
                add(Calendar.MINUTE, offsetMinutes) // Otomatis mundur 10 menit
            }
        }

        // Jika waktu untuk hari ini sudah terlewat, jadwalkan otomatis untuk besok
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
