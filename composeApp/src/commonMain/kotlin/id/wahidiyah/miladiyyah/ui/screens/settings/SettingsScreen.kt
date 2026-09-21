package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.core.data.sync.RemoteCacheStore
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.utils.AppCache
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel

private const val KEY_AUTO_SYNC = "SETTING_AUTO_SYNC"

@Composable
fun SettingsScreen(
    autoSyncEnabled: Boolean,
    onAutoSyncChanged: (Boolean) -> Unit,
    onSyncNow: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }

    val hijriCache = remember(cacheCleared) {
        RemoteCacheStore.loadData(RemoteSyncCoordinator.KEY_HIJRI)
    }
    val kegiatanCache = remember(cacheCleared) {
        RemoteCacheStore.loadData(RemoteSyncCoordinator.KEY_KEGIATAN)
    }
    val pengumumanCache = remember(cacheCleared) {
        RemoteCacheStore.loadData(RemoteSyncCoordinator.KEY_PENGUMUMAN)
    }
    val pustakaCache = remember(cacheCleared) {
        RemoteCacheStore.loadData(RemoteSyncCoordinator.KEY_PUSTAKA)
    }

    val hasCache =
        !hijriCache.isNullOrBlank() ||
        !kegiatanCache.isNullOrBlank() ||
        !pengumumanCache.isNullOrBlank() ||
        !pustakaCache.isNullOrBlank()

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSizes.screenHorizontal,
                    vertical = AppSpacing.xxl
                )
        ) {
            AppSectionLabel(
                "SINKRONISASI",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = BrandPrimary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sinkronisasi otomatis",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (autoSyncEnabled) {
                                    "Data akan diperbarui otomatis."
                                } else {
                                    "Sinkronisasi otomatis dimatikan."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = {
                                AppCache.saveBoolean(KEY_AUTO_SYNC, it)
                                onAutoSyncChanged(it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onSyncNow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sinkronkan Sekarang")
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "DATA LOKAL",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = BrandPrimary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cache lokal",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (hasCache) {
                                    "Data tersimpan di perangkat dan dapat dipakai saat offline."
                                } else {
                                    "Belum ada data cache tersimpan."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasCache
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bersihkan Cache")
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.section))

            AppSectionLabel(
                "STATUS",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Sinkronisasi otomatis: " +
                            if (autoSyncEnabled) "Aktif" else "Nonaktif",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cache lokal: " +
                            if (hasCache) "Tersedia" else "Kosong",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Bersihkan Cache?")
            },
            text = {
                Text(
                    "Semua data cache sinkronisasi lokal akan dihapus. " +
                        "Data dari server tidak ikut terhapus."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        RemoteCacheStore.clear(RemoteSyncCoordinator.KEY_HIJRI)
                        RemoteCacheStore.clear(RemoteSyncCoordinator.KEY_KEGIATAN)
                        RemoteCacheStore.clear(RemoteSyncCoordinator.KEY_PENGUMUMAN)
                        RemoteCacheStore.clear(RemoteSyncCoordinator.KEY_PUSTAKA)
                        cacheCleared = !cacheCleared
                        showClearDialog = false
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}
