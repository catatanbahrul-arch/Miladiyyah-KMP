package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Pengaturan",
            subtitle = "Data dan sinkronisasi aplikasi"
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            AppSectionLabel(
                "STATUS DATA",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Data lokal tetap menjadi sumber utama aplikasi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pengaturan detail sinkronisasi dan preferensi akan dihubungkan ke modul masing-masing tanpa mengubah struktur data yang sudah berjalan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "PREFERENSI",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Tema, ukuran teks, aksesibilitas, dan notifikasi tetap berada di area pengaturan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "UI ini menyiapkan hierarki visualnya terlebih dahulu tanpa menambahkan aksi palsu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
