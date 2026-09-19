package id.wahidiyah.miladiyyah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import id.wahidiyah.miladiyyah.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Jika HP baru dinyalakan ulang (Restart), pasang kembali semua alarm
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.scheduleAll(context)
            return
        }

        val audioType = intent.getStringExtra("AUDIO_TYPE") ?: return

        // JALUR 1: POP-UP NOTIFIKASI 10 MENIT SEBELUM ADZAN (TANPA SUARA ADZAN)
        if (audioType.startsWith("popup_")) {
            val prayerName = audioType.removePrefix("popup_")
            showNotification(context, "Persiapan Salat $prayerName", "Waktu $prayerName kurang 10 menit lagi. Mari bersiap!")
            return
        }

        // JALUR 2: PEMUTARAN SUARA MP3
        val audioResId = when {
            audioType.startsWith("tasyafuan") -> R.raw.tasyafuan
            audioType.startsWith("danabox") -> R.raw.danabox
            audioType.startsWith("tarhim") -> R.raw.tarhim
            audioType.startsWith("adzan") -> R.raw.adzan
            else -> return
        }

        try {
            val mediaPlayer = MediaPlayer.create(context, audioResId)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
            
            // Munculkan notifikasi pop-up saat adzan/suara berbunyi
            val title = if(audioType.startsWith("adzan")) "Waktu Salat Telah Tiba" else "Pengingat Miladiyyah"
            showNotification(context, title, "Mari beribadah & raih keberkahan.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Jadwalkan ulang rantai alarm untuk hari esok
        AlarmScheduler.scheduleAll(context)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "miladiyyah_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Jamaah", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
