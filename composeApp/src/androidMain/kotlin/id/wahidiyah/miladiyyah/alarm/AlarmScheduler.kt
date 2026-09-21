package id.wahidiyah.miladiyyah.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    const val NOTIFICATION_CHANNEL_ID =
        "WAHIDIYAH_ALARM_CHANNEL"

    const val NIDA_NOTIFICATION_CHANNEL_ID =
        "WAHIDIYAH_NIDA_TEXT_CHANNEL"

    const val PRAYER_PRE_ALERT_CHANNEL_ID =
        "WAHIDIYAH_PRAYER_PRE_ALERT_CHANNEL"

    private const val NOTIFICATION_CHANNEL_NAME =
        "Alarm Wahidiyah"

    private const val NOTIFICATION_CHANNEL_DESCRIPTION =
        "Pemberitahuan waktu salat dan pengingat Wahidiyah"

    // Cache lokasi hanya menjadi sumber kontinuitas alarm setelah
    // lokasi perangkat pernah divalidasi. Saat aplikasi dibuka,
    // startup selalu mengambil GPS terbaru lagi.

    private const val MAX_LOCATION_ACCURACY_METERS =
        250.0

    @Volatile
    private var initializedContext: Context? = null

    private const val ID_SUBUH = 101
    private const val ID_DZUHUR = 102
    private const val ID_ASHAR = 103
    private const val ID_MAGHRIB = 104
    private const val ID_ISYA = 105

    private const val ID_PRE_IMSAK = 601
    private const val ID_PRE_SUBUH = 602
    private const val ID_PRE_TERBIT = 603
    private const val ID_PRE_DZUHUR = 604
    private const val ID_PRE_ASHAR = 605
    private const val ID_PRE_MAGHRIB = 606
    private const val ID_PRE_ISYA = 607

    private const val ID_TARHIM = 201
    private const val ID_TASYAFUAN = 301
    private const val ID_DANABOX_MORNING = 401
    private const val ID_DANABOX_EVENING = 402
    private const val ID_NIDA = 501

    fun initialize(context: Context) {
        initializedContext =
            context.applicationContext
    }

    fun rescheduleInitialized() {
        initializedContext?.let { context ->
            rescheduleAllEnabled(context)
        }
    }

    fun prepareNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        NOTIFICATION_CHANNEL_DESCRIPTION
                    setShowBadge(true)
                }

            manager.createNotificationChannel(channel)

            val preAlertChannel =
                NotificationChannel(
                    PRAYER_PRE_ALERT_CHANNEL_ID,
                    "Pengingat Waktu Shalat",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        "Peringatan 10 menit sebelum Imsak, waktu shalat, dan Terbit"
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(true)
                }

            manager.createNotificationChannel(preAlertChannel)
        }
    }

    fun areNotificationsReady(
        context: Context
    ): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            if (
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (!manager.areNotificationsEnabled()) {
            return false
        }

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
        ) {
            val channel =
                manager.getNotificationChannel(
                    NOTIFICATION_CHANNEL_ID
                )

            if (
                channel != null &&
                channel.importance ==
                    NotificationManager.IMPORTANCE_NONE
            ) {
                return false
            }
        }

        return true
    }

    fun cancelAllKnownAlarms(
        context: Context
    ) {
        cancelAdzan(context)
        cancelPrayerPreAlerts(context)
        cancelTarhim(context)
        cancelTasyafuan(context)
        cancelDanaBox(context)
        cancelNida(context)
    }

    fun rescheduleAllEnabled(
        context: Context
    ) {
        initialize(context)

        Log.d(
            TAG,
            "Sinkronisasi ulang seluruh jadwal alarm."
        )

        // Selalu hapus alarm lama lebih dulu.
        cancelAllKnownAlarms(context)

        // Tidak ada alarm jika lokasi belum tervalidasi.
        if (
            !restoreValidatedCachedLocation(
                context
            )
        ) {
            Log.w(
                TAG,
                "Lokasi cached belum valid/fresh."
            )
            return
        }

        if (!areNotificationsReady(context)) {
            Log.w(
                TAG,
                "Notifikasi belum siap."
            )
            return
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.w(
                TAG,
                "Exact alarm belum aktif."
            )
            return
        }

        prepareNotificationChannel(context)

        val isAdzanOn =
            loadBooleanFromPreferences(
                context,
                "ALARM_ADZAN",
                true
            )

        val isTarhimOn =
            loadBooleanFromPreferences(
                context,
                "ALARM_TARHIM",
                true
            )

        val isTasyafuanOn =
            loadBooleanFromPreferences(
                context,
                "ALARM_TASYAFUAN",
                true
            )

        val isDanaBoxOn =
            loadBooleanFromPreferences(
                context,
                "ALARM_DANABOX",
                true
            )

        val isNidaOn =
            loadBooleanFromPreferences(
                context,
                "ALARM_NIDAA",
                true
            )

        if (isNidaOn) {
            scheduleNidaNext(context)
        }

        if (isAdzanOn) {
            scheduleAdzan(context)
            schedulePrayerPreAlerts(context)
        }

        if (isTarhimOn) {
            scheduleTarhim(context)
        }

        if (isTasyafuanOn) {
            scheduleTasyafuan(context)
        }

        if (isDanaBoxOn) {
            scheduleDanaBox(context)
        }
    }

    private fun loadBooleanFromPreferences(
        context: Context,
        key: String,
        default: Boolean
    ): Boolean {
        val prefs =
            context.getSharedPreferences(
                "wahidiyah_cache",
                Context.MODE_PRIVATE
            )

        return prefs
            .getString(key, null)
            ?.toBooleanStrictOrNull()
            ?: default
    }

    private fun restoreValidatedCachedLocation(
        context: Context
    ): Boolean {
        val prefs =
            context.getSharedPreferences(
                "wahidiyah_cache",
                Context.MODE_PRIVATE
            )

        val ready =
            prefs.getString(
                "LOCATION_READY",
                null
            )?.toBooleanStrictOrNull()
                ?: false

        val precise =
            prefs.getString(
                "LOCATION_PRECISE",
                null
            )?.toBooleanStrictOrNull()
                ?: false

        val updatedAt =
            prefs.getString(
                "LOCATION_UPDATED_AT",
                null
            )?.toLongOrNull()
                ?: 0L

        val accuracy =
            prefs.getString(
                "LOCATION_ACCURACY_METERS",
                null
            )?.toDoubleOrNull()
                ?: Double.POSITIVE_INFINITY

        val latitude =
            prefs.getString(
                "LOCATION_LATITUDE",
                null
            )?.toDoubleOrNull()

        val longitude =
            prefs.getString(
                "LOCATION_LONGITUDE",
                null
            )?.toDoubleOrNull()

        val elevation =
            prefs.getString(
                "LOCATION_ELEVATION",
                null
            )?.toDoubleOrNull()
                ?.takeIf {
                    it.isFinite() &&
                        it >= 0.0
                }
                ?: 0.0

        val name =
            prefs.getString(
                "LOCATION_NAME",
                null
            )?.takeIf {
                it.isNotBlank()
            }

        if (
            !ready ||
            !precise ||
            latitude == null ||
            longitude == null ||
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0 ||
            updatedAt <= 0L ||
            !accuracy.isFinite() ||
            accuracy > MAX_LOCATION_ACCURACY_METERS
        ) {
            return false
        }

        PrayerTimeEngine.updateLocation(
            latitude = latitude,
            longitude = longitude,
            name = name ?: "Lokasi GPS",
            elevationMeters = elevation,
            accuracyMeters = accuracy,
            updatedAtEpochMillis = updatedAt,
            source =
                prefs.getString(
                    "LOCATION_SOURCE",
                    null
                ) ?: "CACHE"
        )

        return true
    }

    private fun setAlarm(
        context: Context,
        id: Int,
        actionStr: String,
        title: String,
        timeInMillis: Long
    ) {
        if (
            timeInMillis <=
                System.currentTimeMillis()
        ) {
            return
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {
                action = actionStr
                putExtra(
                    "ALARM_TITLE",
                    title
                )
                putExtra(
                    "ALARM_ID",
                    id
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.e(
                TAG,
                "Exact alarm permission ditolak."
            )
            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )

            Log.d(
                TAG,
                "Alarm dijadwalkan: $title -> $timeInMillis"
            )
        } catch (e: SecurityException) {
            Log.e(
                TAG,
                "SecurityException: ${e.message}"
            )
        }
    }

    private fun cancelSpecificAlarm(
        context: Context,
        id: Int,
        actionStr: String
    ) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {
                action = actionStr
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
    }

    private fun localDateTimeToMillis(
        date: LocalDate,
        time: LocalTime
    ): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(
                Calendar.MONTH,
                date.monthNumber - 1
            )
            set(
                Calendar.DAY_OF_MONTH,
                date.dayOfMonth
            )
            set(
                Calendar.HOUR_OF_DAY,
                time.hour
            )
            set(
                Calendar.MINUTE,
                time.minute
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }.timeInMillis
    }

    private fun scheduleAdzan(
        context: Context
    ) {
        try {
            val tz =
                TimeZone.currentSystemDefault()

            val now =
                Clock.System.now()
                    .toLocalDateTime(tz)

            val today =
                now.date

            val tomorrow =
                today.plus(
                    1,
                    DateTimeUnit.DAY
                )

            val todayPrayers =
                PrayerTimeEngine
                    .getPrayers(today)

            val tomorrowPrayers =
                PrayerTimeEngine
                    .getPrayers(tomorrow)

            val prayerTypes =
                listOf(
                    PrayerType.SUBUH,
                    PrayerType.DZUHUR,
                    PrayerType.ASHAR,
                    PrayerType.MAGHRIB,
                    PrayerType.ISYA
                )

            for (type in prayerTypes) {
                val todayPrayer =
                    todayPrayers.firstOrNull {
                        it.type == type
                    }

                val todayMillis =
                    todayPrayer?.let {
                        localDateTimeToMillis(
                            today,
                            it.time
                        )
                    }

                val (id, action) =
                    adzanDefinition(type)

                if (
                    todayMillis != null &&
                    todayMillis >
                        System.currentTimeMillis()
                ) {
                    setAlarm(
                        context,
                        id,
                        action,
                        "Adzan ${type.title}",
                        todayMillis
                    )
                } else {
                    val tomorrowPrayer =
                        tomorrowPrayers.firstOrNull {
                            it.type == type
                        }

                    if (
                        tomorrowPrayer != null
                    ) {
                        setAlarm(
                            context,
                            id,
                            action,
                            "Adzan ${type.title}",
                            localDateTimeToMillis(
                                tomorrow,
                                tomorrowPrayer.time
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Gagal sinkronisasi Adzan: ${e.message}"
            )
        }
    }

    private fun adzanDefinition(
        type: PrayerType
    ): Pair<Int, String> {
        return when (type) {
            PrayerType.SUBUH ->
                Pair(
                    ID_SUBUH,
                    "ACTION_ADZAN_SUBUH"
                )

            PrayerType.DZUHUR ->
                Pair(
                    ID_DZUHUR,
                    "ACTION_ADZAN_DZUHUR"
                )

            PrayerType.ASHAR ->
                Pair(
                    ID_ASHAR,
                    "ACTION_ADZAN_ASHAR"
                )

            PrayerType.MAGHRIB ->
                Pair(
                    ID_MAGHRIB,
                    "ACTION_ADZAN_MAGHRIB"
                )

            PrayerType.ISYA ->
                Pair(
                    ID_ISYA,
                    "ACTION_ADZAN_ISYA"
                )

            else ->
                Pair(
                    0,
                    ""
                )
        }
    }

    private fun schedulePrayerPreAlerts(
        context: Context
    ) {
        try {
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(tz)
            val today = now.date
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val todayPrayers = PrayerTimeEngine.getPrayers(today)
            val tomorrowPrayers = PrayerTimeEngine.getPrayers(tomorrow)
            val prayerTypes = listOf(
                PrayerType.IMSAK,
                PrayerType.SUBUH,
                PrayerType.TERBIT,
                PrayerType.DZUHUR,
                PrayerType.ASHAR,
                PrayerType.MAGHRIB,
                PrayerType.ISYA
            )

            for (type in prayerTypes) {
                val id = prayerPreAlertId(type) ?: continue
                val todayPrayer = todayPrayers.firstOrNull { it.type == type }
                val todayPreAlert = todayPrayer?.let {
                    localDateTimeToMillis(today, it.time) - 10L * 60L * 1000L
                }

                if (todayPreAlert != null && todayPreAlert > System.currentTimeMillis()) {
                    setAlarm(context, id, "ACTION_PRAYER_PRE_ALERT", "10 menit lagi ${type.title}", todayPreAlert)
                    continue
                }

                val tomorrowPrayer = tomorrowPrayers.firstOrNull { it.type == type }
                val tomorrowPreAlert = tomorrowPrayer?.let {
                    localDateTimeToMillis(tomorrow, it.time) - 10L * 60L * 1000L
                }

                if (tomorrowPreAlert != null && tomorrowPreAlert > System.currentTimeMillis()) {
                    setAlarm(context, id, "ACTION_PRAYER_PRE_ALERT", "10 menit lagi ${type.title}", tomorrowPreAlert)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal sinkronisasi pre-alert waktu shalat: ${e.message}")
        }
    }

    private fun prayerPreAlertId(type: PrayerType): Int? {
        return when (type) {
            PrayerType.IMSAK -> ID_PRE_IMSAK
            PrayerType.SUBUH -> ID_PRE_SUBUH
            PrayerType.TERBIT -> ID_PRE_TERBIT
            PrayerType.DZUHUR -> ID_PRE_DZUHUR
            PrayerType.ASHAR -> ID_PRE_ASHAR
            PrayerType.MAGHRIB -> ID_PRE_MAGHRIB
            PrayerType.ISYA -> ID_PRE_ISYA
            else -> null
        }
    }

    private fun scheduleTarhim(
        context: Context
    ) {
        try {
            val tz =
                TimeZone.currentSystemDefault()

            val now =
                Clock.System.now()
                    .toLocalDateTime(tz)

            val today =
                now.date

            val tomorrow =
                today.plus(
                    1,
                    DateTimeUnit.DAY
                )

            val todaySubuh =
                PrayerTimeEngine
                    .getPrayers(today)
                    .firstOrNull {
                        it.type ==
                            PrayerType.SUBUH
                    }

            val todayAlarm =
                todaySubuh?.let {
                    localDateTimeToMillis(
                        today,
                        it.time
                    ) -
                        15L *
                            60L *
                            1000L
                }

            if (
                todayAlarm != null &&
                todayAlarm >
                    System.currentTimeMillis()
            ) {
                setAlarm(
                    context,
                    ID_TARHIM,
                    "ACTION_TARHIM",
                    "Persiapan Subuh (Tarhim)",
                    todayAlarm
                )
                return
            }

            val tomorrowSubuh =
                PrayerTimeEngine
                    .getPrayers(tomorrow)
                    .firstOrNull {
                        it.type ==
                            PrayerType.SUBUH
                    }

            if (
                tomorrowSubuh != null
            ) {
                val tomorrowAlarm =
                    localDateTimeToMillis(
                        tomorrow,
                        tomorrowSubuh.time
                    ) -
                        15L *
                            60L *
                            1000L

                setAlarm(
                    context,
                    ID_TARHIM,
                    "ACTION_TARHIM",
                    "Persiapan Subuh (Tarhim)",
                    tomorrowAlarm
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Gagal sinkronisasi Tarhim: ${e.message}"
            )
        }
    }

    fun scheduleNidaNext(
        context: Context
    ) {
        initialize(context)

        val enabled =
            loadBooleanFromPreferences(
                context,
                "ALARM_NIDAA",
                true
            )

        if (!enabled) {
            cancelNida(context)
            return
        }

        if (!areNotificationsReady(context)) {
            Log.w(
                TAG,
                "Nida' tidak dijadwalkan: notifikasi belum siap."
            )
            return
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        if (
            Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.w(
                TAG,
                "Nida' tidak dijadwalkan: exact alarm belum aktif."
            )
            return
        }

        val next =
            Calendar.getInstance().apply {
                val currentMinute =
                    get(Calendar.MINUTE)

                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (currentMinute < 30) {
                    set(Calendar.MINUTE, 30)
                } else {
                    set(Calendar.MINUTE, 0)
                    add(Calendar.HOUR_OF_DAY, 1)
                }
            }

        val intent =
            Intent(
                context,
                NidaReceiver::class.java
            ).apply {
                action = "ACTION_NIDA_30_MIN"
                putExtra("ALARM_ID", ID_NIDA)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                ID_NIDA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pendingIntent
            )

            Log.d(
                TAG,
                "Nida' berikutnya: ${next.time}"
            )
        } catch (e: SecurityException) {
            Log.e(
                TAG,
                "Gagal menjadwalkan Nida': ${e.message}"
            )
        }
    }

    private fun cancelNida(
        context: Context
    ) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                NidaReceiver::class.java
            ).apply {
                action = "ACTION_NIDA_30_MIN"
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                ID_NIDA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleTasyafuan(
        context: Context
    ) {
        val cal =
            Calendar.getInstance().apply {
                set(
                    Calendar.HOUR_OF_DAY,
                    3
                )
                set(
                    Calendar.MINUTE,
                    0
                )
                set(
                    Calendar.SECOND,
                    0
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )

                if (
                    timeInMillis <=
                        System.currentTimeMillis()
                ) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        setAlarm(
            context,
            ID_TASYAFUAN,
            "ACTION_TASYAFUAN",
            "Pengingat Tasyafu'an",
            cal.timeInMillis
        )
    }

    private fun scheduleDanaBox(
        context: Context
    ) {
        val morning =
            Calendar.getInstance().apply {
                set(
                    Calendar.HOUR_OF_DAY,
                    6
                )
                set(
                    Calendar.MINUTE,
                    0
                )
                set(
                    Calendar.SECOND,
                    0
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )

                if (
                    timeInMillis <=
                        System.currentTimeMillis()
                ) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        val evening =
            Calendar.getInstance().apply {
                set(
                    Calendar.HOUR_OF_DAY,
                    19
                )
                set(
                    Calendar.MINUTE,
                    0
                )
                set(
                    Calendar.SECOND,
                    0
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )

                if (
                    timeInMillis <=
                        System.currentTimeMillis()
                ) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        setAlarm(
            context,
            ID_DANABOX_MORNING,
            "ACTION_DANABOX",
            "Dana Box Pagi",
            morning.timeInMillis
        )

        setAlarm(
            context,
            ID_DANABOX_EVENING,
            "ACTION_DANABOX",
            "Dana Box Malam",
            evening.timeInMillis
        )
    }

    private fun cancelAdzan(
        context: Context
    ) {
        cancelSpecificAlarm(
            context,
            ID_SUBUH,
            "ACTION_ADZAN_SUBUH"
        )
        cancelSpecificAlarm(
            context,
            ID_DZUHUR,
            "ACTION_ADZAN_DZUHUR"
        )
        cancelSpecificAlarm(
            context,
            ID_ASHAR,
            "ACTION_ADZAN_ASHAR"
        )
        cancelSpecificAlarm(
            context,
            ID_MAGHRIB,
            "ACTION_ADZAN_MAGHRIB"
        )
        cancelSpecificAlarm(
            context,
            ID_ISYA,
            "ACTION_ADZAN_ISYA"
        )
    }

    private fun cancelPrayerPreAlerts(context: Context) {
        val ids = listOf(
            ID_PRE_IMSAK,
            ID_PRE_SUBUH,
            ID_PRE_TERBIT,
            ID_PRE_DZUHUR,
            ID_PRE_ASHAR,
            ID_PRE_MAGHRIB,
            ID_PRE_ISYA
        )
        ids.forEach { id ->
            cancelSpecificAlarm(
                context,
                id,
                "ACTION_PRAYER_PRE_ALERT"
            )
        }
    }

    private fun cancelTarhim(
        context: Context
    ) {
        cancelSpecificAlarm(
            context,
            ID_TARHIM,
            "ACTION_TARHIM"
        )
    }

    private fun cancelTasyafuan(
        context: Context
    ) {
        cancelSpecificAlarm(
            context,
            ID_TASYAFUAN,
            "ACTION_TASYAFUAN"
        )
    }

    private fun cancelDanaBox(
        context: Context
    ) {
        cancelSpecificAlarm(
            context,
            ID_DANABOX_MORNING,
            "ACTION_DANABOX"
        )
        cancelSpecificAlarm(
            context,
            ID_DANABOX_EVENING,
            "ACTION_DANABOX"
        )
    }
}
