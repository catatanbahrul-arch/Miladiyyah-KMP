package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.utils.UiState
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel
import kotlinx.coroutines.launch

@Composable
fun PustakaScreen() {
    var pustakaList by remember {
        mutableStateOf(RemoteSyncCoordinator.loadCachedPustaka())
    }

    var isLoading by remember {
        mutableStateOf(pustakaList.isEmpty())
    }

    LaunchedEffect(Unit) {
        isLoading = pustakaList.isEmpty()
        pustakaList = RemoteSyncCoordinator.syncPustaka()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Pustaka Jamaah",
            subtitle = "Kitab dan arsip digital"
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            AppSectionLabel(
                "DOKUMEN JAMAAH",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                }

                pustakaList.isEmpty() -> {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(BrandAccentLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "Pustaka kosong",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Belum ada dokumen yang tersedia.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pustakaList.forEach { item: PustakaItem ->
                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )

                                    if (item.content.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            item.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.35
                                        )
                                    }

                                    if (item.link.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            "Dokumen tersedia",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = BrandPrimary
                                        )
                                        Text(
                                            item.link,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
