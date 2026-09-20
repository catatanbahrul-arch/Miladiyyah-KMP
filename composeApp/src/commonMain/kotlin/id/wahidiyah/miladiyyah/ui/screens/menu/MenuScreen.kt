package id.wahidiyah.miladiyyah.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import id.wahidiyah.miladiyyah.AppScreen
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel

@Composable
private fun MenuRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(AppSizes.iconCircle)
                .clip(CircleShape)
                .background(BrandAccentLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BrandPrimary)
        }

        Spacer(modifier = Modifier.width(AppSpacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}

@Composable
fun MenuScreen(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Menu",
            subtitle = "Fitur tambahan dan pengaturan aplikasi"
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            AppSectionLabel(
                "FITUR WAHIDIYAH",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    MenuRow(
                        title = "Pustaka Jamaah",
                        subtitle = "Kitab dan arsip digital",
                        icon = Icons.Default.Info,
                        onClick = { onNavigate(AppScreen.PUSTAKA) }
                    )
                    HorizontalDivider(color = Background)
                    MenuRow(
                        title = "Kompas Kiblat",
                        subtitle = "Arah kiblat berbasis lokasi",
                        icon = Icons.Default.LocationOn,
                        onClick = { onNavigate(AppScreen.KIBLAT) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "PENGATURAN",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                MenuRow(
                    title = "Pengaturan Umum",
                    subtitle = "Data dan sinkronisasi lokal",
                    icon = Icons.Default.Settings,
                    onClick = { onNavigate(AppScreen.PENGATURAN) }
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))
        }
    }
}
