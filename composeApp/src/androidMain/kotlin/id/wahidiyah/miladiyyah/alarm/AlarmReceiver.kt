package id.wahidiyah.miladiyyah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val action =
            intent.action ?: return

        val title =
            intent.getStringExtra(
                "ALARM_TITLE"
            ) ?: "Waktu Tiba"

        val alarmId =
            intent.getIntExtra(
                "ALARM_ID",
                0
            )

        // Setelah alarm berbunyi, cari occurrence berikutnya
        // pada hari ini atau besok.
        AlarmScheduler.rescheduleAllEnabled(
            context
        )

        val serviceIntent =
            Intent(
                context,
                AudioService::class.java
            ).apply {
                this.action = action
                putExtra(
                    "ALARM_TITLE",
                    title
                )
                putExtra(
                    "ALARM_ID",
                    alarmId
                )
            }

        try {
            if (
                Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
            ) {
                context.startForegroundService(
                    serviceIntent
                )
            } else {
                context.startService(
                    serviceIntent
                )
            }
        } catch (_: Exception) {
            // Alarm sudah di-reschedule. Audio service dapat
            // ditolak OS dalam kondisi background tertentu.
        }
    }
}
