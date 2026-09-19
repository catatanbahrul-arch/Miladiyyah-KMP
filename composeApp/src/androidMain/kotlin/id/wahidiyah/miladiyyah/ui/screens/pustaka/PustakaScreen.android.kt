package id.wahidiyah.miladiyyah.ui.screens.pustaka

import android.content.Intent
import android.net.Uri
import java.accessibility.AccessibilityManager

actual fun openUrl(url: String) {
    try {
        val context = android.content.Intent.ACTION_VIEW // Placeholder context or handled via platform
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
