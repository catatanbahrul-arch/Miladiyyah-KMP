package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.utils.AppCache
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit
) {
    var adzanEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true))
    }
    var tarhimEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true))
    }
    var tasyafuanEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true))
    }
    var danaBoxEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true))
    }
    var nidaEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("ALARM_NIDAA", true))
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        Surface(color = BrandAccentLight, tonalElevation = 0.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = BrandPrimaryDark)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengaturan Pengingat", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = BrandPrimaryDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Atur semua pengingat ibadah dan kegiatan", fontSize = 12.sp, color = BrandPrimary)
                }
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.padding(end = 12.dp).size(25.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            ReminderCard("Adzan & Sholat", "Peringatan masuk waktu", adzanEnabled) {
                adzanEnabled = it
                AppCache.saveBoolean("ALARM_ADZAN", it)
                updateAlarmSchedules()
            }
            ReminderDivider()
            ReminderCard("Pengingat Tarhim", "Sebelum waktu Subuh", tarhimEnabled) {
                tarhimEnabled = it
                AppCache.saveBoolean("ALARM_TARHIM", it)
                updateAlarmSchedules()
            }
            ReminderDivider()
            ReminderCard("Tasyafu'an", "Setiap 03:00 WIB", tasyafuanEnabled) {
                tasyafuanEnabled = it
                AppCache.saveBoolean("ALARM_TASYAFUAN", it)
                updateAlarmSchedules()
            }
            ReminderDivider()
            ReminderCard("Dana Box", "Pukul 06:00 & 19:00", danaBoxEnabled) {
                danaBoxEnabled = it
                AppCache.saveBoolean("ALARM_DANABOX", it)
                updateAlarmSchedules()
            }
            ReminderDivider()
            ReminderCard("Pengingat Nida'", "Setiap 30 menit • tanpa suara", nidaEnabled) {
                nidaEnabled = it
                AppCache.saveBoolean("ALARM_NIDAA", it)
                updateAlarmSchedules()
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Surface, MaterialTheme.shapes.large)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary)
        )
    }
}

@Composable
private fun ReminderDivider() {
    Spacer(modifier = Modifier.height(8.dp))
}
