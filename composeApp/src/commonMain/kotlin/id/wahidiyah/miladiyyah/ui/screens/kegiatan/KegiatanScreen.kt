package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import id.wahidiyah.miladiyyah.AppRepository
import id.wahidiyah.miladiyyah.core.utils.UiState
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel
import kotlinx.coroutines.launch

@Composable
fun KegiatanScreen() {
    var uiState by remember { mutableStateOf<UiState<String>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        uiState = AppRepository.getKegiatan()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Kegiatan",
            subtitle = "Informasi resmi kegiatan Wahidiyah"
        )

        Column(
            modifier = Modifier.padding(
                horizontal = AppSizes.screenHorizontal,
                vertical = AppSpacing.xxl
            )
        ) {
            AppSectionLabel(
                "AGENDA RESMI",
                modifier = Modifier.padding(start = 4.dp, bottom = AppSpacing.md)
            )

            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                }

                is UiState.Empty -> {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(BrandAccentLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "Belum ada agenda",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Belum ada agenda kegiatan baru.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    val err = uiState as UiState.Error

                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Terjadi kesalahan",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                err.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        uiState = UiState.Loading
                                        uiState = AppRepository.getKegiatan()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandPrimary
                                )
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                "Informasi Kegiatan",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                (uiState as UiState.Success).data,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35
                            )
                        }
                    }
                }
            }
        }
    }
}
