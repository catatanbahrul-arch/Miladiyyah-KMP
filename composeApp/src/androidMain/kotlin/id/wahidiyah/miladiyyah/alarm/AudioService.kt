package id.wahidiyah.miladiyyah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.wahidiyah.miladiyyah.MainActivity

class AudioService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = AlarmScheduler.NOTIFICATION_CHANNEL_ID

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val title = intent?.getStringExtra("ALARM_TITLE") ?: "Pemberitahuan Wahidiyah"
        val alarmId = intent?.getIntExtra("ALARM_ID", 1001) ?: 1001

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Menggunakan ikon aplikasi (Akar masalah nomor 1 diselesaikan di sini)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wahidiyah")
            .setContentText(title)
            .setSmallIcon(resources.getIdentifier("ic_launcher_foreground", "drawable", packageName).takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        startForeground(alarmId, notification)

        // Penanganan Mencegah Multiple Audio Bentrok (Duplikat)
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        val audioResource = when {
            action?.contains("ADZAN") == true -> "adzan"
            action == "ACTION_TARHIM" -> "tarhim"
            action == "ACTION_TASYAFUAN" -> "tasyafuan"
            action == "ACTION_DANABOX" -> "danabox"
            else -> null
        }

        if (audioResource != null) {
            val resId = resources.getIdentifier(audioResource, "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId).apply {
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                        stopForeground(true)
                        stopSelf() // WAJIB: Mematikan Service agar baterai aman
                    }
                    start()
                }
            } else {
                stopForeground(true)
                stopSelf()
            }
        } else {
            stopForeground(true)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Alarm Wahidiyah", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Pemberitahuan Waktu Salat dan Pengingat" }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
