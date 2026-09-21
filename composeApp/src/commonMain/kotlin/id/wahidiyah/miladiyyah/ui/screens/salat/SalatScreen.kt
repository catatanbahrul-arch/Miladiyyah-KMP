package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@OptIn(ExperimentalMaterial3Api::class)
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
            .height(360.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF18B8A5),
                            Color(0xFF0B8A7C)
                        )
                    )
                )
        )

        MosqueHeaderSilhouette(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 22.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "Jadwal Sholat",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showShareSheet = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        ShareArrowIcon,
                        contentDescription = "Bagikan",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Informasi",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = { showPrayerSettings = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFF4D5C),
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = compactPrayerLocation(PrayerTimeEngine.locationName),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (nextPrayer != null) {
                Text(
                    text =
                        "${nextPrayer.prayer.type.title} " +
                            "${nextPrayer.prayer.time.hour.toString().padStart(2, '0')}:" +
                            nextPrayer.prayer.time.minute.toString().padStart(2, '0') +
                            " WIB",
                    color = Color.White,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "- ${formatCountdown(countdownSeconds)}",
                    color = Color.White,
                    fontSize = 23.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onUpdateLocation) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Update",
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }

                TextButton(onClick = onNavigateToKiblat) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Arah Kiblat",
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 58.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { dayOffset -= 1 },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Hari sebelumnya",
                        tint = Color(0xFF178F83),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedDateLabel,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (hijriLabel.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hijriLabel,
                            fontSize = 17.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { dayOffset += 1 },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Hari berikutnya",
                        tint = Color(0xFF178F83),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(68.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        prayers.forEach { prayer ->
            val isCurrent =
                nextPrayer?.prayer?.type == prayer.type &&
                    nextPrayer.date == selectedDate

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isCurrent) BrandAccentLight
                        else Color.Transparent
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 15.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = prayerVisualIcon(prayer.type),
                        contentDescription = prayer.type.title,
                        tint = if (prayer.type == PrayerType.IMSAK) {
                            TextMuted
                        } else if (isCurrent) {
                            BrandPrimary
                        } else {
                            TextSecondary
                        },
                        modifier = Modifier.size(29.dp)
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = prayer.type.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (prayer.type == PrayerType.IMSAK) {
                            TextSecondary
                        } else {
                            TextPrimary
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text =
                            "${prayer.time.hour.toString().padStart(2, '0')}:" +
                                prayer.time.minute.toString().padStart(2, '0'),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Icon(
                        imageVector = if (prayer.type == PrayerType.IMSAK) {
                            Icons.Default.VolumeOff
                        } else {
                            Icons.Default.VolumeUp
                        },
                        contentDescription = if (prayer.type == PrayerType.IMSAK) {
                            "Pengingat tidak aktif"
                        } else {
                            "Pengingat aktif"
                        },
                        tint = if (prayer.type == PrayerType.IMSAK) {
                            TextMuted
                        } else {
                            TextSecondary
                        },
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
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



private fun compactPrayerLocation(name: String): String {
    val cleaned = name.trim()
    if (cleaned.isBlank()) return "Lokasi belum siap"

    return cleaned
        .substringBefore(", Kabupaten")
        .substringBefore(", Kota")
        .replace("Kecamatan ", "Kec. ")
        .replace("Kecamatan", "Kec.")
        .trim()
        .removeSuffix(",")
}

private fun prayerVisualIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.IMSAK -> Icons.Default.Nightlight
        PrayerType.SUBUH -> Icons.Default.Cloud
        PrayerType.TERBIT -> Icons.Default.WbTwilight
        PrayerType.DHUHA -> Icons.Default.WbSunny
        PrayerType.DZUHUR -> Icons.Default.WbSunny
        PrayerType.ASHAR -> Icons.Default.Cloud
        PrayerType.MAGHRIB -> Icons.Default.WbTwilight
        PrayerType.ISYA -> Icons.Default.Nightlight
    }
}

@Composable
private fun MosqueHeaderSilhouette(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = Color.White.copy(alpha = 0.10f)
        val baseY = size.height * 0.88f

        drawRect(
            color = c,
            topLeft = Offset(0f, baseY),
            size = Size(size.width, size.height - baseY)
        )

        val domeFractions = listOf(0.10f, 0.28f, 0.50f, 0.72f, 0.90f)
        domeFractions.forEachIndexed { index, fraction ->
            val radius = if (index == 2) {
                size.width * 0.075f
            } else {
                size.width * 0.055f
            }
            val x = size.width * fraction

            drawCircle(
                color = c,
                radius = radius,
                center = Offset(x, baseY)
            )

            drawRect(
                color = c,
                topLeft = Offset(x - radius, baseY),
                size = Size(radius * 2f, size.height * 0.13f)
            )
        }

        listOf(0.19f, 0.39f, 0.61f, 0.81f).forEach { fraction ->
            val x = size.width * fraction
            val towerWidth = size.width * 0.012f
            val towerHeight = size.height * 0.43f
            val towerTop = baseY - towerHeight

            drawRect(
                color = c,
                topLeft = Offset(x - towerWidth, towerTop),
                size = Size(towerWidth * 2f, towerHeight)
            )

            drawCircle(
                color = c,
                radius = towerWidth * 2.3f,
                center = Offset(x, towerTop)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
