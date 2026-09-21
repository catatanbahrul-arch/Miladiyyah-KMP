package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTime
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.core.utils.AppCache
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel
import kotlinx.coroutines.delay
import kotlinx.datetime.*

expect fun updateAlarmSchedules()

private val ShareArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ShareArrowIcon",
        defaultWidth = Dp(24f),
        defaultHeight = Dp(24f),
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = androidx.compose.ui.graphics.SolidColor(
                androidx.compose.ui.graphics.Color.White
            )
        ) {
            moveTo(18f, 16.08f)
            curveTo(17.24f, 16.08f, 16.56f, 16.38f, 16.04f, 16.85f)
            lineTo(8.91f, 12.7f)
            curveTo(8.96f, 12.47f, 9f, 12.24f, 9f, 12f)
            curveTo(9f, 11.76f, 8.96f, 11.53f, 8.91f, 11.3f)
            lineTo(15.96f, 7.19f)
            curveTo(16.49f, 7.69f, 17.21f, 8f, 18f, 8f)
            curveTo(19.66f, 8f, 21f, 6.66f, 21f, 5f)
            curveTo(21f, 3.34f, 19.66f, 2f, 18f, 2f)
            curveTo(16.34f, 2f, 15f, 3.34f, 15f, 5f)
            curveTo(15f, 5.24f, 15.04f, 5.47f, 15.09f, 5.7f)
            lineTo(8.04f, 9.81f)
            curveTo(7.51f, 9.31f, 6.79f, 9f, 6f, 9f)
            curveTo(4.34f, 9f, 3f, 10.34f, 3f, 12f)
            curveTo(3f, 13.66f, 4.34f, 15f, 6f, 15f)
            curveTo(6.79f, 15f, 7.51f, 14.69f, 8.04f, 14.19f)
            lineTo(15.16f, 18.34f)
            curveTo(15.11f, 18.55f, 15.08f, 18.77f, 15.08f, 19f)
            curveTo(15.08f, 20.66f, 16.42f, 22f, 18.08f, 22f)
            curveTo(19.74f, 22f, 21.08f, 20.66f, 21.08f, 19f)
            curveTo(21.08f, 17.34f, 19.74f, 16.08f, 18f, 16.08f)
            close()
        }
    }.build()

private data class NextPrayerTarget(
    val date: LocalDate,
    val prayer: PrayerTime
)

private fun resolveNextPrayer(
    now: Instant,
    timeZone: TimeZone
): NextPrayerTarget? {
    val today = Clock.System.todayIn(timeZone)

    fun candidatesFor(date: LocalDate): List<PrayerTime> {
        return try {
            PrayerTimeEngine.getPrayers(date).filter {
                it.type == PrayerType.SUBUH ||
                    it.type == PrayerType.DZUHUR ||
                    it.type == PrayerType.ASHAR ||
                    it.type == PrayerType.MAGHRIB ||
                    it.type == PrayerType.ISYA
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    candidatesFor(today).firstOrNull {
        today.atTime(it.time).toInstant(timeZone) > now
    }?.let {
        return NextPrayerTarget(today, it)
    }

    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    return candidatesFor(tomorrow).firstOrNull()?.let {
        NextPrayerTarget(tomorrow, it)
    }
}

private fun formatCountdown(seconds: Long): String {
    val safe = maxOf(0L, seconds)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val secs = safe % 60

    return buildString {
        append(hours.toString().padStart(2, '0'))
        append(" : ")
        append(minutes.toString().padStart(2, '0'))
        append(" : ")
        append(secs.toString().padStart(2, '0'))
    }
}

@Composable
fun SalatScreen(
    onNavigateToKiblat: () -> Unit = {},
    onBack: () -> Unit = {},
    onUpdateLocation: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val timeZone = TimeZone.currentSystemDefault()

    var nowInstant by remember { mutableStateOf(Clock.System.now()) }
    var dayOffset by remember { mutableStateOf(0) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showPrayerSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            nowInstant = Clock.System.now()
            delay(1000L)
        }
    }

    val today = Clock.System.todayIn(timeZone)
    val selectedDate = today.plus(dayOffset, DateTimeUnit.DAY)
    val isToday = selectedDate == today

    val prayers = try {
        PrayerTimeEngine.getPrayers(selectedDate)
    } catch (_: Exception) {
        emptyList()
    }

    val nextPrayer = if (isToday) {
        resolveNextPrayer(nowInstant, timeZone)
    } else {
        null
    }

    val calendarDay = remember(selectedDate) {
        try {
            CalendarEngine().getCalendarDay(selectedDate)
        } catch (_: Exception) {
            null
        }
    }

    val dayNames = listOf(
        "Senin", "Selasa", "Rabu", "Kamis",
        "Jumat", "Sabtu", "Ahad"
    )

    val monthNames = listOf(
        "", "Januari", "Februari", "Maret", "April", "Mei",
        "Juni", "Juli", "Agustus", "September", "Oktober",
        "November", "Desember"
    )

    val hijriMonthNames = listOf(
        "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir",
        "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syakban",
        "Ramadan", "Syawal", "Zulkaidah", "Zulhijjah"
    )

    val selectedDateLabel =
        "${dayNames[selectedDate.dayOfWeek.ordinal]}, " +
            "${selectedDate.dayOfMonth} ${monthNames[selectedDate.monthNumber]} " +
            "${selectedDate.year}"

    val hijriLabel = calendarDay?.hijri?.let {
        "${it.day} ${hijriMonthNames[it.month]} ${it.year}"
    } ?: ""

    val countdownSeconds = nextPrayer?.let {
        (
            it.date.atTime(it.prayer.time)
                .toInstant(timeZone)
                .toEpochMilliseconds() -
                nowInstant.toEpochMilliseconds()
        ) / 1000L
    } ?: 0L

    var adzanEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true))
    }
    var tarhimEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true))
    }
    var tasyafuanEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true))
    }
    var danaBoxEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true))
    }
    var nidaEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_NIDAA", false))
    }

    if (showPrayerSettings) {
        PrayerSettingsScreen(
            onBack = { showPrayerSettings = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(BrandPrimary, BrandPrimaryDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 10.dp,
                        bottom = 18.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Surface
                        )
                    }

                    Text(
                        text = "Jadwal Sholat",
                        color = Surface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = ShareArrowIcon,
                            contentDescription = "Bagikan",
                            tint = Surface
                        )
                    }

                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Informasi",
                            tint = Surface
                        )
                    }

                    IconButton(
                        onClick = { showPrayerSettings = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = Surface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Surface,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = PrayerTimeEngine.locationName,
                        color = Surface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onUpdateLocation) {
                        Text(
                            "Update",
                            color = Surface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(onClick = onNavigateToKiblat) {
                        Text(
                            "Arah Kiblat",
                            color = Surface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = 18.dp
            )
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { dayOffset -= 1 }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Hari sebelumnya",
                            tint = BrandPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedDateLabel,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (hijriLabel.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hijriLabel,
                                fontSize = 16.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = { dayOffset += 1 }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Hari berikutnya",
                            tint = BrandPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            if (nextPrayer != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "SHOLAT BERIKUTNYA",
                    fontSize = 15.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = BrandAccentLight
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp)
                    ) {
                        Text(
                            text = nextPrayer.prayer.type.title.uppercase(),
                            color = BrandPrimary,
                            fontSize = 14.sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text =
                                "${nextPrayer.prayer.time.hour.toString().padStart(2, '0')}:" +
                                    nextPrayer.prayer.time.minute.toString().padStart(2, '0') +
                                    " WIB",
                            color = BrandPrimaryDark,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = formatCountdown(countdownSeconds),
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Menuju waktu ${nextPrayer.prayer.type.title}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(9.dp))

                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isToday) {
                    "JADWAL SHOLAT HARI INI"
                } else {
                    "JADWAL SHOLAT"
                },
                fontSize = 15.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    prayers.forEachIndexed { index, prayer ->
                        val isCurrent =
                            nextPrayer?.prayer?.type == prayer.type &&
                                nextPrayer.date == selectedDate

                        val isInactive =
                            prayer.type == PrayerType.IMSAK ||
                                prayer.type == PrayerType.TERBIT ||
                                prayer.type == PrayerType.DHUHA

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (isCurrent) BrandAccentLight else Surface
                                )
                                .padding(horizontal = 10.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(
                                        when {
                                            isCurrent -> BrandPrimary
                                            isInactive -> Border
                                            else -> BrandAccent
                                        },
                                        CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = prayer.type.title,
                                fontSize = 17.sp,
                                color = if (isInactive) TextSecondary else TextPrimary,
                                fontWeight =
                                    if (isCurrent) FontWeight.Bold
                                    else FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text =
                                    "${prayer.time.hour.toString().padStart(2, '0')}:" +
                                        prayer.time.minute.toString().padStart(2, '0'),
                                fontSize = 19.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            if (!isInactive) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint =
                                        if (isCurrent) BrandPrimary else TextMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        if (index < prayers.lastIndex) {
                            HorizontalDivider(
                                color = Background,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "PENGATURAN PENGINGAT",
                modifier = Modifier.padding(
                    start = 4.dp,
                    bottom = AppSpacing.md
                )
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    AlarmSettingRow(
                        title = "Adzan & Sholat",
                        subtitle = "Peringatan masuk waktu",
                        checked = adzanEnabled,
                        onCheckedChange = {
                            adzanEnabled = it
                            AppCache.saveBoolean("ALARM_ADZAN", it)
                            updateAlarmSchedules()
                        }
                    )

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    AlarmSettingRow(
                        title = "Pengingat Tarhim",
                        subtitle = "Sebelum waktu Subuh",
                        checked = tarhimEnabled,
                        onCheckedChange = {
                            tarhimEnabled = it
                            AppCache.saveBoolean("ALARM_TARHIM", it)
                            updateAlarmSchedules()
                        }
                    )

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    AlarmSettingRow(
                        title = "Tasyafu'an",
                        subtitle = "Setiap 03:00 WIB",
                        checked = tasyafuanEnabled,
                        onCheckedChange = {
                            tasyafuanEnabled = it
                            AppCache.saveBoolean("ALARM_TASYAFUAN", it)
                            updateAlarmSchedules()
                        }
                    )

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    AlarmSettingRow(
                        title = "Dana Box",
                        subtitle = "Pukul 06:00 & 19:00",
                        checked = danaBoxEnabled,
                        onCheckedChange = {
                            danaBoxEnabled = it
                            AppCache.saveBoolean("ALARM_DANABOX", it)
                            updateAlarmSchedules()
                        }
                    )

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    AlarmSettingRow(
                        title = "Pengingat Nida'",
                        subtitle = "Setiap 30 menit • tanpa suara",
                        checked = nidaEnabled,
                        onCheckedChange = {
                            nidaEnabled = it
                            AppCache.saveBoolean("ALARM_NIDAA", it)
                            updateAlarmSchedules()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))
        }
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showShareSheet = false
                        }
                        .padding(
                            horizontal = 28.dp,
                            vertical = 18.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ShareArrowIcon,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = "Bagikan jadwal shalat hari ini",
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showShareSheet = false
                        }
                        .padding(
                            horizontal = 28.dp,
                            vertical = 18.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = "Lihat jadwal shalat bulanan",
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text("Pengingat")
            },
            text = {
                Text(
                    "Waktu shalat pada aplikasi ini hanya membantu. " +
                        "Untuk memastikan akurasi waktu, silakan cek " +
                        "perhitungan jadwal shalat di daerah masing-masing."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}

@Composable
private fun AlarmSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = BrandPrimary
            )
        )
    }
}

@Composable
private fun PrayerSettingsScreen(
    onBack: () -> Unit
) {
    var automaticCalculation by remember { mutableStateOf(true) }

    val latitudeText = formatDms(
        PrayerTimeEngine.latitude,
        isLatitude = true
    )

    val longitudeText = formatDms(
        PrayerTimeEngine.longitude,
        isLatitude = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Surface
                    )
                }

                Text(
                    text = "Pengaturan Waktu Shalat",
                    color = Surface,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 32.dp,
                    vertical = 28.dp
                )
            ) {
                Text(
                    text = "Lokasi Waktu Shalat",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Lokasi",
                        fontSize = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = PrayerTimeEngine.locationName,
                        fontSize = 17.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Koordinat: $latitudeText  $longitudeText",
                    fontSize = 17.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ketinggian: ${PrayerTimeEngine.elevationMeters.toInt()} meter",
                    fontSize = 17.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text =
                        "Data ketinggian diperoleh otomatis dari koordinat lokasi " +
                            "Anda, sehingga waktu maghrib dan terbit ditampilkan " +
                            "lebih akurat.",
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    color = TextSecondary
                )
            }

            HorizontalDivider(
                color = Background,
                thickness = 12.dp
            )

            Column(
                modifier = Modifier.padding(
                    horizontal = 32.dp,
                    vertical = 28.dp
                )
            ) {
                Text(
                    text = "Perhitungan Waktu Shalat",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan Otomatis",
                        fontSize = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = automaticCalculation,
                        onCheckedChange = {
                            automaticCalculation = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = BrandAccent,
                            checkedThumbColor = Surface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lembaga Falakiyah NU, Indonesia",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "✓",
                        color = BrandPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatDms(
    value: Double,
    isLatitude: Boolean
): String {
    val absolute = kotlin.math.abs(value)
    val degrees = absolute.toInt()
    val minutesFloat = (absolute - degrees) * 60.0
    val minutes = minutesFloat.toInt()
    val seconds = ((minutesFloat - minutes) * 60.0).toInt()

    val direction = if (isLatitude) {
        if (value >= 0) "LU" else "LS"
    } else {
        if (value >= 0) "BT" else "BB"
    }

    return "${degrees}°${minutes}'${seconds}\" $direction"
}
