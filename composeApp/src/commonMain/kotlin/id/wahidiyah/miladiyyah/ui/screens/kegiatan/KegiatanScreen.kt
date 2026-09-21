package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanItem
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.data.source.remote.normalizeKegiatanDate
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.components.AppHeader
import id.wahidiyah.miladiyyah.ui.components.AppSectionLabel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val KegiatanDayNames = listOf(
    "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
)

private val KegiatanMonthNames = listOf(
    "",
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

private data class ParsedKegiatan(
    val item: KegiatanItem,
    val date: LocalDate
)

private data class KegiatanMonthSection(
    val year: Int,
    val month: Int,
    val items: List<KegiatanItem>,
    val isFallback: Boolean = false
)

private fun formatKegiatanDateIndonesian(raw: String): String {
    val normalized = normalizeKegiatanDate(raw)
    val date = runCatching { LocalDate.parse(normalized) }.getOrNull()
        ?: return raw.substringBefore(" GMT").trim()

    val weekday = KegiatanDayNames[date.dayOfWeek.value - 1]
    val month = KegiatanMonthNames[date.monthNumber]

    return "$weekday, ${date.dayOfMonth} $month ${date.year}"
}

private fun parseKegiatanDate(item: KegiatanItem): LocalDate? {
    val normalized = normalizeKegiatanDate(item.date)
    return runCatching { LocalDate.parse(normalized) }.getOrNull()
}

private fun buildKegiatanMonthSections(
    source: List<KegiatanItem>
): List<KegiatanMonthSection> {
    val parsed = source.mapNotNull { item ->
        parseKegiatanDate(item)?.let { date ->
            ParsedKegiatan(
                item = item,
                date = date
            )
        }
    }

    val grouped = parsed
        .groupBy { item ->
            item.date.year to item.date.monthNumber
        }
        .entries
        .sortedWith(
            compareBy(
                { it.key.first },
                { it.key.second }
            )
        )
        .map { (key, values) ->
            KegiatanMonthSection(
                year = key.first,
                month = key.second,
                items = values
                    .sortedBy { it.date }
                    .map { it.item }
            )
        }

    val parsedDates = parsed
        .map { it.item }
        .toSet()

    val fallback = source
        .filterNot { it in parsedDates }

    return if (fallback.isEmpty()) {
        grouped
    } else {
        grouped + KegiatanMonthSection(
            year = Int.MAX_VALUE,
            month = 0,
            items = fallback,
            isFallback = true
        )
    }
}

private fun currentKegiatanMonth(): Pair<Int, Int> {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return now.year to now.monthNumber
}

private fun findInitialMonthSectionIndex(
    sections: List<KegiatanMonthSection>
): Int {
    if (sections.isEmpty()) return 0

    val current = currentKegiatanMonth()

    val sameOrNext = sections.indexOfFirst { section ->
        !section.isFallback &&
            (
                section.year > current.first ||
                    (section.year == current.first && section.month >= current.second)
            )
    }

    return if (sameOrNext >= 0) {
        sameOrNext
    } else {
        val fallbackIndex = sections.indexOfFirst { it.isFallback }
        if (fallbackIndex >= 0) fallbackIndex else sections.lastIndex
    }
}

@Composable
fun KegiatanScreen() {
    var kegiatanList by remember {
        mutableStateOf(RemoteSyncCoordinator.loadCachedKegiatan())
    }

    var isLoading by remember {
        mutableStateOf(kegiatanList.isEmpty())
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        isLoading = kegiatanList.isEmpty()
        kegiatanList = RemoteSyncCoordinator.syncKegiatan()
        isLoading = false
    }

    val monthSections = remember(kegiatanList) {
        buildKegiatanMonthSections(kegiatanList)
    }

    LaunchedEffect(monthSections) {
        if (monthSections.isNotEmpty()) {
            // Item 0 = "AGENDA RESMI".
            // Header bulan dimulai dari item 1.
            val targetSection = findInitialMonthSectionIndex(monthSections)
            listState.scrollToItem(targetSection + 1)
        }
    }

    val (currentYear, currentMonth) = currentKegiatanMonth()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "Kegiatan",
            subtitle = "Informasi resmi kegiatan Wahidiyah"
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
            }

            kegiatanList.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = AppSizes.screenHorizontal,
                            vertical = AppSpacing.xxl
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = AppSizes.screenHorizontal,
                        vertical = AppSpacing.xxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AppSectionLabel(
                            "AGENDA RESMI",
                            modifier = Modifier.padding(
                                start = 4.dp,
                                bottom = 2.dp
                            )
                        )
                    }

                    monthSections.forEach { section ->
                        val isCurrentMonth =
                            section.year == currentYear &&
                                section.month == currentMonth

                        item(
                            key = "month-${section.year}-${section.month}"
                        ) {
                            AppSectionLabel(
                                if (section.isFallback) {
                                    "AGENDA LAINNYA"
                                } else {
                                    "AGENDA ${KegiatanMonthNames[section.month]} ${section.year}"
                                },
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    top = if (isCurrentMonth) 2.dp else 8.dp,
                                    bottom = 0.dp
                                )
                            )
                        }

                        items(section.items) { item: KegiatanItem ->
                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BrandAccentLight)
                                            .padding(
                                                horizontal = 12.dp,
                                                vertical = 10.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            formatKegiatanDateIndonesian(item.date),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = BrandPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (item.location.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                item.location,
                                                style = MaterialTheme.typography.bodyMedium,
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
}
