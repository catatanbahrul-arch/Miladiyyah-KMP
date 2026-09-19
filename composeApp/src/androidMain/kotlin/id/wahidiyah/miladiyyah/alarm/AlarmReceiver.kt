package id.wahidiyah.miladiyyah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import id.wahidiyah.miladiyyah.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Menerima pesan alarm tipe apa yang sedang berbunyi
        val audioType = intent.getStringExtra("AUDIO_TYPE") ?: return
        
        // Mencocokkan tipe dengan file mp3 di folder raw
        val audioResId = when (audioType) {
            "tasyafuan" -> R.raw.tasyafuan
            "danabox" -> R.raw.danabox
            "tarhim" -> R.raw.tarhim
            "adzan" -> R.raw.adzan
            else -> return
        }

        // Memutar suara
        try {
            val mediaPlayer = MediaPlayer.create(context, audioResId)
            mediaPlayer.start()
            // Membersihkan memori setelah audio selesai berbunyi
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
