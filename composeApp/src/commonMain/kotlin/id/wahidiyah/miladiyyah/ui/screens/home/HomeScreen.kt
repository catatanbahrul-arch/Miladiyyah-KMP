package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.WahidiyahLogo
import kotlinx.coroutines.delay
import kotlinx.datetime.*


private data class LocalPromoItem(
    val text: String
)

private object LocalPromoData {
    val items = listOf(
        LocalPromoItem("Sudah Berdana BOX hari ini?"),
        LocalPromoItem("Sudah Mujahadah hari ini?"),
        LocalPromoItem("Bacalah selalu YAASAYIDII YAARASUULALLAH")
    )
}

@Composable
private fun LocalPromoCarousel() {
    val messages = remember {
        LocalPromoData.items
    }

    var currentIndex by remember { mutableStateOf(0) }
    var dragAmount by remember { mutableStateOf(0f) }

    LaunchedEffect(currentIndex) {
        delay(5000L)
        currentIndex = (currentIndex + 1) % messages.size
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandAccentLight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmount = 0f },
                    onHorizontalDrag = { _, amount ->
                        dragAmount += amount
                    },
                    onDragEnd = {
                        when {
                            dragAmount <= -60f -> {
                                currentIndex = (currentIndex + 1) % messages.size
                            }
                            dragAmount >= 60f -> {
                                currentIndex =
                                    (currentIndex - 1 + messages.size) % messages.size
                            }
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith
                            slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith
                            slideOutHorizontally { it }
                    }
                },
                label = "local_promo_carousel"
            ) { index ->
                Text(
                    text = messages[index].text,
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                messages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == currentIndex) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) {
                                    BrandPrimary
                                } else {
                                    BrandPrimary.copy(alpha = 0.25f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToSalat: () -> Unit = {},
    onNavigateToPustaka: () -> Unit = {},
    onUpdateLocation: () -> Unit = {},
    onNavigateToAllFeatures: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
    var pengumuman by remember { mutableStateOf(RemoteSyncCoordinator.loadCachedPengumuman()) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        pengumuman = RemoteSyncCoordinator.syncPengumuman()
        while (true) {
            currentDateTime = Clock.System.now().toLocalDateTime(tz)
            delay(1000L)
        }
    }

    var dayOffset by remember { mutableStateOf(0) }
    val targetDate = Clock.System.todayIn(tz).plus(dayOffset, DateTimeUnit.DAY)
    val nextPrayer = try {
        PrayerTimeEngine.getNextPrayer(currentDateTime.time)
    } catch (e: Exception) {
        null
    }

    val monthNames = listOf(
        "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    val dateString =
        "${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]} ${targetDate.year}"

    val hijriBase = CalendarEngine().getToday().hijri
    val tempDay = hijriBase.day + dayOffset
    val hDay = when {
        tempDay > 30 -> tempDay % 30
        tempDay <= 0 -> 30 + (tempDay % 30)
        else -> tempDay
    }
    val hMonthNames = listOf(
        "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir",
        "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban",
        "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        // ----------------------------------------------------
        // HERO / BRANDING
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    BrandAccentLight,
                    shape = RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
                )
                .padding(
                    top = 28.dp,
                    bottom = 44.dp,
                    start = 24.dp,
                    end = 24.dp
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                WahidiyahLogo(modifier = Modifier.width(176.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .clickable { onUpdateLocation() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Perbarui lokasi",
                        tint = BrandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        PrayerTimeEngine.locationName,
                        color = BrandPrimaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (nextPrayer != null) {
                    val diff =
                        (nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60) -
                            (currentDateTime.time.hour * 3600 +
                                currentDateTime.time.minute * 60 +
                                currentDateTime.time.second)
                    val dSecs = if (diff >= 0) diff else diff + 86400

                    Text(
                        nextPrayer.type.title.uppercase(),
                        color = BrandPrimary,
                        fontSize = 13.sp,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "${nextPrayer.time.hour.toString().padStart(2, '0')}:${nextPrayer.time.minute.toString().padStart(2, '0')}",
                        color = BrandPrimaryDark,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "- ${dSecs / 3600}:${((dSecs % 3600) / 60).toString().padStart(2, '0')}:${(dSecs % 60).toString().padStart(2, '0')}",
                        color = BrandPrimaryDark.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // ----------------------------------------------------
        // IMPORTANT ANNOUNCEMENT
        // ----------------------------------------------------
        // Pengumuman aktif menjadi secondary-hero dan muncul
        // tepat setelah area hero. Jika tidak ada data, blok ini
        // tidak dirender sehingga Home kembali bersih.
        // ----------------------------------------------------
        if (
            pengumuman != null &&
            (
                pengumuman!!.title.isNotBlank() ||
                    pengumuman!!.content.isNotBlank()
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 4.dp,
                        bottom = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        bottom = 10.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        "PENGUMUMAN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandPrimary,
                        letterSpacing = 1.6.sp
                    )
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandPrimary
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(BrandAccent)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 20.dp
                                )
                        ) {
                            Text(
                                if (
                                    pengumuman!!.title.isNotBlank()
                                ) {
                                    pengumuman!!.title
                                } else {
                                    "Pengumuman"
                                },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Surface,
                                lineHeight = 26.sp
                            )

                            if (
                                pengumuman!!.content.isNotBlank()
                            ) {
                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )

                                Text(
                                    pengumuman!!.content,
                                    fontSize = 14.sp,
                                    color = Surface.copy(
                                        alpha = 0.92f
                                    ),
                                    lineHeight = 22.sp
                                )
                            }

                            val announcementLink =
                                pengumuman!!.link.trim()

                            if (announcementLink.isNotBlank()) {
                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )

                                Button(
                                    onClick = {
                                        uriHandler.openUri(announcementLink)
                                    },
                                    enabled =
                                        announcementLink.startsWith("https://") ||
                                            announcementLink.startsWith("http://"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandAccentLight,
                                        contentColor = BrandPrimaryDark,
                                        disabledContainerColor = BrandAccentLight.copy(
                                            alpha = 0.45f
                                        ),
                                        disabledContentColor = BrandPrimaryDark.copy(
                                            alpha = 0.55f
                                        )
                                    )
                                ) {
                                    Text(
                                        "Lihat selengkapnya →",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------
        // LOCAL TEXT CAROUSEL / PREVIEW
        // ----------------------------------------------------

        // ----------------------------------------------------
        // TODAY / DATE CARD
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp)
                .padding(horizontal = 24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { dayOffset -= 1 }
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Hari sebelumnya",
                            tint = BrandPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dateString,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$hDay ${hMonthNames[hijriBase.month]} ${hijriBase.year} H",
                            fontSize = 13.sp,
                            color = BrandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { dayOffset += 1 }
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Hari berikutnya",
                            tint = BrandPrimary
                        )
                    }
                }
            }
        }

        LocalPromoCarousel()

        // ----------------------------------------------------
        // SEMUA FITUR
        // ----------------------------------------------------
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-8).dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAllFeatures() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BrandAccentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Semua Fitur",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Jadwal sholat, kompas, kegiatan, pustaka & pengingat",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
