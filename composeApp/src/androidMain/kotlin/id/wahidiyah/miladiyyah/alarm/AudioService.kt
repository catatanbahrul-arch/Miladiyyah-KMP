package id.wahidiyah.miladiyyah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import id.wahidiyah.miladiyyah.R

class AudioService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioType = intent?.getStringExtra("AUDIO_TYPE") ?: return START_NOT_STICKY

        val audioResId = when {
            audioType.startsWith("tasyafuan") -> R.raw.tasyafuan
            audioType.startsWith("danabox") -> R.raw.danabox
            audioType.startsWith("tarhim") -> R.raw.tarhim
            audioType.startsWith("adzan") -> R.raw.adzan
            else -> return START_NOT_STICKY
        }

        // Buat Notifikasi Khusus Pemutar Media agar HP tidak membunuh proses ini
        val channelId = "miladiyyah_audio_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pemutar Suara", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }
            .setContentTitle(if (audioType.startsWith("adzan")) "Waktu Salat Tiba" else "Pengingat Miladiyyah")
            .setContentText("Memutar suara...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        // 2 = FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1001, notification, 2) 
            } else {
                startForeground(1001, notification)
            }
        } catch (e: Exception) {
            startForeground(1001, notification)
        }

        try {
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                stopSelf() // Matikan service otomatis setelah suara selesai
            }
        } catch (e: Exception) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }
}
