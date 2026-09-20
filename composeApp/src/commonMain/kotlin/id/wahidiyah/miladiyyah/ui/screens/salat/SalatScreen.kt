package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.core.utils.AppCache
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel
import kotlinx.datetime.*

expect fun updateAlarmSchedules()

@Composable
fun SalatScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val today = Clock.System.todayIn(tz)

    var dayOffset by remember { mutableStateOf(0) }
    val selectedDate =
        today.plus(dayOffset, DateTimeUnit.DAY)
    val isToday = selectedDate == today

    val prayers = try {
        PrayerTimeEngine.getPrayers(selectedDate)
    } catch (e: Exception) {
        emptyList()
    }

    val nextPrayer =
        if (isToday) {
            try {
                PrayerTimeEngine.getNextPrayer(now.time)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

    val dayNames = listOf(
        "Senin", "Selasa", "Rabu", "Kamis",
        "Jumat", "Sabtu", "Ahad"
    )

    val monthNames = listOf(
        "", "Januari", "Februari", "Maret", "April",
        "Mei", "Juni", "Juli", "Agustus", "September",
        "Oktober", "November", "Desember"
    )

    val selectedDateLabel =
        "${dayNames[selectedDate.dayOfWeek.ordinal]}, " +
            "${selectedDate.dayOfMonth} " +
            "${monthNames[selectedDate.monthNumber]} " +
            "${selectedDate.year}"

    var adzanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true)) }
    var tarhimEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true)) }
    var tasyafuanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true)) }
    var danaBoxEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true)) }
    var nidaEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_NIDAA", false)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        AppHeader(
            title = "Waktu Sholat",
            subtitle = PrayerTimeEngine.locationName
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp,
                                vertical = 6.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                dayOffset -= 1
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Hari sebelumnya",
                                tint = BrandPrimary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (isToday) {
                                    "Hari Ini"
                                } else {
                                    "Jadwal Sholat"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                selectedDateLabel,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                dayOffset += 1
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Hari berikutnya",
                                tint = BrandPrimary
                            )
                        }
                    }

                    if (!isToday) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    dayOffset = 0
                                }
                            ) {
                                Text(
                                    "Kembali ke hari ini",
                                    color = BrandPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(AppSpacing.section)
            )

            if (nextPrayer != null) {
                AppSectionLabel(
                    "SHOLAT BERIKUTNYA",
                    modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
                )

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = BrandAccentLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Text(
                            nextPrayer.type.title.uppercase(),
                            color = BrandPrimary,
                            fontSize = 13.sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${nextPrayer.time.hour.toString().padStart(2, '0')}:${nextPrayer.time.minute.toString().padStart(2, '0')}",
                            color = BrandPrimaryDark,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.section))
            }

            AppSectionLabel(
                if (isToday) {
                    "JADWAL SHOLAT HARI INI"
                } else {
                    "JADWAL SHOLAT"
                },
                modifier = Modifier.padding(
                    start = 4.dp,
                    bottom = AppSpacing.md
                )
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) {
                    prayers.forEachIndexed { index, prayer ->
                        val isSilent =
                            prayer.type == PrayerType.IMSAK ||
                                prayer.type == PrayerType.TERBIT ||
                                prayer.type == PrayerType.DHUHA

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isSilent) Border else BrandAccent,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                prayer.type.title,
                                fontSize = 16.sp,
                                color = if (isSilent) TextSecondary else TextPrimary,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${prayer.time.hour.toString().padStart(2, '0')}:${prayer.time.minute.toString().padStart(2, '0')}",
                                fontSize = 18.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (index < prayers.size - 1) {
                            HorizontalDivider(color = Background, thickness = 2.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = BrandPrimary),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToKiblat() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Surface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Buka Kompas Kiblat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "PENGATURAN PENGINGAT",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Adzan & Sholat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Peringatan masuk waktu",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = adzanEnabled,
                            onCheckedChange = {
                                adzanEnabled = it
                                AppCache.saveBoolean("ALARM_ADZAN", it)
                                updateAlarmSchedules()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pengingat Tarhim",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Sebelum waktu Subuh",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = tarhimEnabled,
                            onCheckedChange = {
                                tarhimEnabled = it
                                AppCache.saveBoolean("ALARM_TARHIM", it)
                                updateAlarmSchedules()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Tasyafu'an",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Setiap 03:00 WIB",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = tasyafuanEnabled,
                            onCheckedChange = {
                                tasyafuanEnabled = it
                                AppCache.saveBoolean("ALARM_TASYAFUAN", it)
                                updateAlarmSchedules()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Dana Box",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Pukul 06:00 & 19:00",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = danaBoxEnabled,
                            onCheckedChange = {
                                danaBoxEnabled = it
                                AppCache.saveBoolean("ALARM_DANABOX", it)
                                updateAlarmSchedules()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = Background, thickness = 2.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pengingat Nida'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Setiap 30 menit • tanpa suara",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = nidaEnabled,
                            onCheckedChange = {
                                nidaEnabled = it
                                AppCache.saveBoolean("ALARM_NIDAA", it)
                                updateAlarmSchedules()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))
        }
    }
}
