#!/bin/bash

echo "⚙️ Membangun Mesin Jadwal Salat Otomatis, Imsak, & Pengingat 10 Menit..."

# 1. BUAT ENGINE JADWAL SALAT (Domain Layer)
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/prayer
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/prayer/PrayerTimeEngine.kt
package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.LocalTime

enum class PrayerType(val title: String) {
    IMSAK("Imsak"), 
    SUBUH("Subuh"), 
    DZUHUR("Zuhur"), 
    ASHAR("Ashar"), 
    MAGHRIB("Maghrib"), 
    ISYA("Isyak")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    // Catatan: Ini adalah data simulasi awal yang nantinya akan disambungkan dengan library GPS Kemenag/MABIMS.
    // Saat ini diset mendekati waktu Nganjuk/Kediri agar UI bisa langsung berjalan dan dites.
    fun getTodayPrayers(): List<PrayerTime> {
        val subuh = LocalTime(4, 15)
        
        // LOGIKA IMSAK OTOMATIS: 10 Menit sebelum Subuh
        val subuhMins = subuh.hour * 60 + subuh.minute
        val imsakMins = subuhMins - 10
        val imsak = LocalTime(imsakMins / 60, imsakMins % 60)
        
        return listOf(
            PrayerTime(PrayerType.IMSAK, imsak),
            PrayerTime(PrayerType.SUBUH, subuh),
            PrayerTime(PrayerType.DZUHUR, LocalTime(11, 35)),
            PrayerTime(PrayerType.ASHAR, LocalTime(14, 48)),
            PrayerTime(PrayerType.MAGHRIB, LocalTime(17, 35)),
            PrayerTime(PrayerType.ISYA, LocalTime(18, 45))
        )
    }

    // Mendapatkan jadwal salat berikutnya berdasarkan waktu saat ini dan status Ramadhan
    fun getNextPrayer(now: LocalTime, isRamadhan: Boolean): PrayerTime {
        val prayers = getTodayPrayers().filter { 
            // Jika bukan Ramadhan, sembunyikan Imsak
            if (!isRamadhan) it.type != PrayerType.IMSAK else true 
        }
        
        return prayers.firstOrNull { 
            (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) 
        } ?: prayers.first() // Jika sudah lewat Isya, kembali ke jadwal pertama esok hari
    }
}
EOF

# 2. UPDATE HOMESCREEN.KT (Menambahkan UI Hitung Mundur, Peringatan 10 Menit, dan Deteksi Ramadhan)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanData
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanRepository
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    var pengumuman by remember { mutableStateOf<PengumumanData?>(null) }
    val scope = rememberCoroutineScope()
    
    // Engine untuk mendeteksi bulan Hijriyah (Ramadhan = bulan ke-9)
    val calendarEngine = remember { CalendarEngine() }
    val todayHijri = remember { calendarEngine.getToday().hijri }
    val isRamadhan = todayHijri.month == 9

    // State untuk Waktu dan Hitung Mundur Salat
    var currentTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time) }
    
    LaunchedEffect(Unit) {
        scope.launch { pengumuman = PengumumanRepository.fetchPengumuman() }
        
        // Loop untuk memperbarui waktu setiap detik
        while(true) {
            currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            delay(1000L)
        }
    }

    val nextPrayer = PrayerTimeEngine.getNextPrayer(currentTime, isRamadhan)
    val currentMins = currentTime.hour * 60 + currentTime.minute
    val nextMins = nextPrayer.time.hour * 60 + nextPrayer.time.minute
    val diffMins = if (nextMins >= currentMins) nextMins - currentMins else (nextMins + 1440) - currentMins
    
    val isWarningTime = diffMins in 0..10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepForestGreen)
                .padding(20.dp)
        ) {
            Column {
                Text("Jamaah Wahidiyah", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Aplikasi Kalender & Kegiatan Miladiyyah", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // PENGINGAT 10 MENIT SEBELUM ADZAN / IMSAK (Muncul Paling Atas)
            if (isWarningTime) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Persiapan Waktu ${nextPrayer.type.title}", 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                "Kurang $diffMins menit lagi memasuki waktu ${nextPrayer.type.title}.", 
                                fontSize = 12.sp, 
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // KARTU PENGUMUMAN PENTING
            if (pengumuman != null && (!pengumuman!!.title.isEmpty() || !pengumuman!!.content.isEmpty())) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pengumuman!!.title.isNotEmpty()) pengumuman!!.title else "Pengumuman Penting",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pengumuman!!.content,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )

                        if (!pengumuman!!.link.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFFFE082), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Tautan Google Drive */ },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lihat Himbauan Selengkapnya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text(">", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                        }
                    }
                }
            }

            // PREVIEW JADWAL SALAT BERIKUTNYA
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepForestGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (nextPrayer.type == id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType.IMSAK) "Jadwal Berikutnya" else "Salat Berikutnya", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(nextPrayer.type.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val timeString = "${nextPrayer.time.hour.toString().padStart(2, '0')}:${nextPrayer.time.minute.toString().padStart(2, '0')}"
                        Text(timeString, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$diffMins menit lagi", color = SubtleGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // PREVIEW MENU CEPAT BAWAH
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kalender", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    }
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kegiatan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    }
                }
            }
        }
    }
}
EOF

echo "✅ Sistem Jadwal Salat, Deteksi Ramadhan, & Pengingat 10 Menit Berhasil Dipasang!"
EOF
