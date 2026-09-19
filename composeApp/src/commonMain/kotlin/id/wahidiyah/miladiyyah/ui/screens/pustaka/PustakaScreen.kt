package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppCache
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PustakaScreen() {
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var lastUpdated by remember { mutableStateOf(AppCache.load("PUSTAKA_LAST_UPDATE") ?: "-") }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pustaka Jamaah", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Update terakhir: $lastUpdated", color = TextSecondary, fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true
                            delay(1500L) 
                            val currentDate = "Baru saja" 
                            AppCache.save("PUSTAKA_LAST_UPDATE", currentDate)
                            lastUpdated = currentDate
                            isUpdating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccentLight, contentColor = BrandPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sinkron", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        HorizontalDivider(color = Border)

        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LibraryBooks, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Pustaka Offline", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kitab dan materi tersimpan di perangkat.\nTekan Sinkron jika ada penambahan materi dari pusat.",
                    textAlign = TextAlign.Center, 
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
