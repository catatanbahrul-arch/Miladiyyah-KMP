package id.wahidiyah.miladiyyah.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import id.wahidiyah.miladiyyah.MainActivity

class NidaReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_ID = 501
        private const val TITLE = "Pengingat Nida'"
        private const val MESSAGE =
            "YAA SAYYIDII YAA ROSUULALLOOH"
    }

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        if (
            intent?.action !=
                "ACTION_NIDA_30_MIN"
        ) {
            return
        }

        postTextNotification(context)

        // Satu slot selesai. Buat slot 30 menit berikutnya.
        AlarmScheduler.scheduleNidaNext(context)
    }

    private fun postTextNotification(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId =
            AlarmScheduler.NIDA_NOTIFICATION_CHANNEL_ID

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
        ) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Pengingat Nida'",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description =
                        "Pengingat teks Nida' setiap 30 menit"
                    setSound(
                        null,
                        null
                    )
                    enableVibration(false)
                    setShowBadge(false)
                }

            manager.createNotificationChannel(
                channel
            )
        }

        val contentIntent =
            PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                Intent(
                    context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val smallIcon =
            context.applicationInfo.icon

        val builder =
            if (
                Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
            ) {
                Notification.Builder(
                    context,
                    channelId
                )
            } else {
                Notification.Builder(context)
            }

        builder
            .setSmallIcon(smallIcon)
            .setContentTitle(TITLE)
            .setContentText(MESSAGE)
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(MESSAGE)
            )
            .setPriority(
                Notification.PRIORITY_LOW
            )
            .setCategory(
                Notification.CATEGORY_REMINDER
            )
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
        ) {
            builder.setSilent(true)
        } else {
            builder
                .setSound(null)
                .setVibrate(
                    longArrayOf(0L)
                )
        }

        manager.notify(
            NOTIFICATION_ID,
            builder.build()
        )
    }
}
