package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.core.domain.calendar.model.CalendarDay
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader

@Composable
fun DateDetailScreen(day: CalendarDay, onBack: () -> Unit) {
    val dayNames = listOf("", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val hijriMonths = listOf(
        "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir",
        "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban",
        "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah"
    )
    val monthNames = listOf(
        "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Detail Tanggal",
            subtitle = "Informasi lengkap untuk satu hari",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = BrandAccentLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        dayNames[day.dayOfWeek],
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${day.gregorian.day} ${monthNames[day.gregorian.month]} ${day.gregorian.year}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrandPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hijriyah", color = TextSecondary)
                        Text(
                            "${day.hijri.day} ${hijriMonths[day.hijri.month]} ${day.hijri.year} H",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pasaran", color = TextSecondary)
                        Text(
                            day.pasaran.name,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
            EmptyStateCard("Jadwal Salat belum disinkronkan.")
            EmptyStateCard("Tidak ada kegiatan pada tanggal ini.")
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = BrandPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                message,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
