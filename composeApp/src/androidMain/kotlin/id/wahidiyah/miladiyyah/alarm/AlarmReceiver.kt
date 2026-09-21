package id.wahidiyah.miladiyyah.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import id.wahidiyah.miladiyyah.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    private fun showPrayerPreAlert(
        context: Context,
        text: String,
        alarmId: Int
    ) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                9000 + alarmId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val smallIcon =
            context.resources
                .getIdentifier("ic_launcher_foreground", "drawable", context.packageName)
                .takeIf { it != 0 }
                ?: android.R.drawable.ic_dialog_info

        val notification =
            NotificationCompat.Builder(
                context,
                AlarmScheduler.PRAYER_PRE_ALERT_CHANNEL_ID
            )
                .setSmallIcon(smallIcon)
                .setContentTitle(text)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(null)
                .setVibrate(null)
                .setDefaults(0)
                .build()

        manager.notify(9000 + alarmId, notification)
    }

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

        if (action == "ACTION_PRAYER_PRE_ALERT") {
            showPrayerPreAlert(
                context,
                title,
                alarmId
            )

            // Cari occurrence berikutnya tanpa menjalankan AudioService.
            AlarmScheduler.rescheduleAllEnabled(context)
            return
        }

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
