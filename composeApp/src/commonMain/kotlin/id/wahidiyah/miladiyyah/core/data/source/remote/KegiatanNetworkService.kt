package id.wahidiyah.miladiyyah.core.data.source.remote

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class KegiatanItem(
    val date: String,
    val title: String,
    val location: String = ""
)


fun normalizeKegiatanDate(raw: String): String {
    val value = raw.trim()

    if (value.isBlank()) return value

    // ISO: YYYY-MM-DD atau YYYY-M-D
    Regex("^(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:[T\\s].*)?$")
        .matchEntire(value)
        ?.let {
            val year = it.groupValues[1]
            val month = it.groupValues[2].padStart(2, '0')
            val day = it.groupValues[3].padStart(2, '0')
            return "$year-$month-$day"
        }

    // GAS / JavaScript Date:
    // JavaScript Date string from GAS as plain text...
    Regex("^[A-Za-z]{3}\\s+([A-Za-z]{3})\\s+(\\d{1,2})\\s+(\\d{4}).*$")
        .matchEntire(value)
        ?.let {
            val monthName = it.groupValues[1]
            val day = it.groupValues[2].padStart(2, '0')
            val year = it.groupValues[3]

            val month = when (monthName.lowercase()) {
                "jan", "januari" -> "01"
                "feb", "februari" -> "02"
                "mar", "maret" -> "03"
                "apr", "april" -> "04"
                "may", "mei" -> "05"
                "jun", "juni" -> "06"
                "jul", "juli" -> "07"
                "aug", "agu", "agustus" -> "08"
                "sep", "sept", "september" -> "09"
                "oct", "okt", "oktober" -> "10"
                "nov", "november" -> "11"
                "dec", "des", "desember" -> "12"
                else -> null
            }

            if (month != null) {
                return "$year-$month-$day"
            }
        }

    // DD/MM/YYYY fallback.
    Regex("^(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})$")
        .matchEntire(value)
        ?.let {
            val day = it.groupValues[1].toIntOrNull()
            val month = it.groupValues[2].toIntOrNull()
            val year = it.groupValues[3]

            if (day != null && month != null) {
                return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            }
        }

    return value
}

object KegiatanRepository {
    private val client = HttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private const val BASE_URL =
        "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=kegiatan"

    suspend fun fetchKegiatan(): RemoteFetchResult<List<KegiatanItem>> {
        return try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val response = client.get("$BASE_URL&t=$timestamp") {
                headers {
                    append("Cache-Control", "no-cache, no-store, max-age=0")
                    append("Pragma", "no-cache")
                    append("Accept", "application/json, text/plain, */*")
                }
            }

            val httpCode = response.status.value
            if (!response.status.isSuccess()) {
                return RemoteFetchResult(false, emptyList(), httpCode, "HTTP $httpCode")
            }

            val raw = response.bodyAsText()
            if (raw.isBlank()) {
                return RemoteFetchResult(true, emptyList(), httpCode)
            }

            val root = try {
                json.parseToJsonElement(raw)
            } catch (e: Exception) {
                return RemoteFetchResult(false, emptyList(), httpCode, "JSON error: ${e.message}")
            }

            val array = extractArray(root)

            val result = array.mapNotNull { element ->
                val obj = element as? JsonObject ?: return@mapNotNull null

                val date = first(obj, "date", "tanggal", "tgl", "startDate", "start")
                    ?: return@mapNotNull null

                val title = first(obj, "title", "acara", "kegiatan", "nama", "name", "event")
                    ?: return@mapNotNull null

                KegiatanItem(
                    date = normalizeKegiatanDate(date),
                    title = title.trim(),
                    location = first(obj, "location", "lokasi", "tempat").orEmpty().trim()
                )
            }

            RemoteFetchResult(true, result, httpCode)
        } catch (e: Exception) {
            RemoteFetchResult(false, emptyList(), error = e.message)
        }
    }

    fun normalizeKegiatanDate(raw: String): String {
        val value = raw.trim()

        if (value.isBlank()) return value

        Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})(?:[T\s].*)?$""")
            .matchEntire(value)
            ?.let {
                val year = it.groupValues[1]
                val month = it.groupValues[2].padStart(2, '0')
                val day = it.groupValues[3].padStart(2, '0')
                return "$year-$month-$day"
            }

        Regex("""^[A-Za-z]{3}\s+([A-Za-z]{3})\s+(\d{1,2})\s+(\d{4}).*$""")
            .matchEntire(value)
            ?.let {
                val monthName = it.groupValues[1]
                val day = it.groupValues[2].padStart(2, '0')
                val year = it.groupValues[3]

                val month = when (monthName.lowercase()) {
                    "jan", "januari" -> "01"
                    "feb", "februari" -> "02"
                    "mar", "maret" -> "03"
                    "apr", "april" -> "04"
                    "may", "mei" -> "05"
                    "jun", "juni" -> "06"
                    "jul", "juli" -> "07"
                    "aug", "agu", "agustus" -> "08"
                    "sep", "sept", "september" -> "09"
                    "oct", "okt", "oktober" -> "10"
                    "nov", "november" -> "11"
                    "dec", "des", "desember" -> "12"
                    else -> null
                }

                if (month != null) {
                    return "$year-$month-$day"
                }
            }

        Regex("""^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$""")
            .matchEntire(value)
            ?.let {
                val day = it.groupValues[1].toIntOrNull()
                val month = it.groupValues[2].toIntOrNull()
                val year = it.groupValues[3]

                if (day != null && month != null) {
                    return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                }
            }

        return value
    }

    private fun extractArray(root: JsonElement): JsonArray = when (root) {
        is JsonArray -> root
        is JsonObject -> listOf("data", "items", "kegiatan", "result")
            .asSequence()
            .mapNotNull { root[it] }
            .firstOrNull { it is JsonArray }
            ?.jsonArray
            ?: JsonArray(emptyList())
        else -> JsonArray(emptyList())
    }

    private fun first(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val value = obj[key]?.jsonPrimitive?.contentOrNull
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
