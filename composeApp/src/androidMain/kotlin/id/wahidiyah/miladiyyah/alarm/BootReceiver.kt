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
