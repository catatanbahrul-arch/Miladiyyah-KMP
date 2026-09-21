package id.wahidiyah.miladiyyah.ui.screens.semuafitur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppScreen
import id.wahidiyah.miladiyyah.theme.*

@Composable
private fun FeatureRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = BrandAccentLight,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

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

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}

@Composable
private fun FeatureSection(
    title: String,
    items: List<Triple<String, String, () -> Unit>>,
    icons: List<androidx.compose.ui.graphics.vector.ImageVector>
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = TextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    FeatureRow(
                        title = item.first,
                        subtitle = item.second,
                        icon = icons[index],
                        onClick = item.third
                    )

                    if (index < items.lastIndex) {
                        HorizontalDivider(color = Background)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AllFeaturesScreen(
    onNavigate: (AppScreen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Semua Fitur",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Akses seluruh fitur Wahidiyah",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            FeatureSection(
                title = "IBADAH & PENGINGAT",
                items = listOf(
                    Triple(
                        "Jadwal Sholat",
                        "Waktu sholat berdasarkan lokasi",
                        { onNavigate(AppScreen.SALAT) }
                    ),
                    Triple(
                        "Kompas Kiblat",
                        "Arah kiblat berbasis lokasi",
                        { onNavigate(AppScreen.KIBLAT) }
                    ),
                    Triple(
                        "Pengaturan Pengingat",
                        "Adzan, Tarhim, Tasyafu'an, Dana Box & Nida'",
                        { onNavigate(AppScreen.PENGATURAN_PENGINGAT) }
                    )
                ),
                icons = listOf(
                    Icons.Default.Notifications,
                    Icons.Default.LocationOn,
                    Icons.Default.Notifications
                )
            )

            FeatureSection(
                title = "INFORMASI",
                items = listOf(
                    Triple(
                        "Kalender",
                        "Masehi, Hijriyah dan penyesuaian tanggal",
                        { onNavigate(AppScreen.KALENDER) }
                    ),
                    Triple(
                        "Kegiatan",
                        "Agenda dan kegiatan jamaah",
                        { onNavigate(AppScreen.KEGIATAN) }
                    ),
                    Triple(
                        "Pustaka Jamaah",
                        "Kitab dan arsip digital",
                        { onNavigate(AppScreen.PUSTAKA) }
                    )
                ),
                icons = listOf(
                    Icons.Default.DateRange,
                    Icons.Default.List,
                    Icons.Default.Info
                )
            )

            FeatureSection(
                title = "PENGATURAN",
                items = listOf(
                    Triple(
                        "Pengaturan Umum",
                        "Data dan sinkronisasi aplikasi",
                        { onNavigate(AppScreen.PENGATURAN) }
                    )
                ),
                icons = listOf(Icons.Default.Settings)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
