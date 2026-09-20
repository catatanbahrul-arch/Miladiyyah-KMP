package id.wahidiyah.miladiyyah.ui.screens.salat

import id.wahidiyah.miladiyyah.alarm.AlarmScheduler

actual fun updateAlarmSchedules() {
    // Sinkronkan ulang alarm OS setiap kali pengaturan alarm berubah.
    AlarmScheduler.rescheduleInitialized()
}
