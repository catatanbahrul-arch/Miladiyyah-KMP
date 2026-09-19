package id.wahidiyah.miladiyyah.ui.screens.salat
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler
import id.wahidiyah.miladiyyah.MainActivity

actual fun updateAlarmSchedules() {
    MainActivity.instance?.let { context ->
        AlarmScheduler.rescheduleAllEnabled(context)
    }
}
