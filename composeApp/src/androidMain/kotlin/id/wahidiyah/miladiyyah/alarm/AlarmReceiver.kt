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
