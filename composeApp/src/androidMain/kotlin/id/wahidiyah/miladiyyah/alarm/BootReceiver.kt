package id.wahidiyah.miladiyyah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                // Jangan membaca credential-protected preferences
                // saat device masih locked.
                AlarmScheduler.cancelAllKnownAlarms(
                    context
                )
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                AlarmScheduler.initialize(
                    context
                )

                AlarmScheduler.rescheduleAllEnabled(
                    context
                )
            }

            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // APK baru wajib menunggu MainActivity mendapatkan
                // lokasi fresh + permission sebelum membuat alarm.
                AlarmScheduler.cancelAllKnownAlarms(
                    context
                )
            }
        }
    }
}
