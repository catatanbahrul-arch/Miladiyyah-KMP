package id.wahidiyah.miladiyyah.core.data.source.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class CascadeAdjustment(
    val startDate: String,
    val adjustment: Int,
    val desc: String
)

data class GasCascadeResponse(
    val status: String,
    val data: List<CascadeAdjustment>
)

class CalendarNetworkService {

    private val parser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(parser)
        }
    }

    suspend fun fetchCascadeAdjustments(
        url: String = GasEndpointConfig.KOREKSI_HIJRI
    ): RemoteFetchResult<List<CascadeAdjustment>> {
        return try {
            val ts = Clock.System.now().toEpochMilliseconds()
            val endpoint = if (url.contains("?")) "$url&t=$ts" else "$url?t=$ts"

            println("[HIJRI-SYNC] GET $endpoint")

            val response = client.get(endpoint) {
                headers {
                    append("Cache-Control", "no-cache, no-store, max-age=0")
                    append("Pragma", "no-cache")
                    append("Accept", "application/json, text/plain, */*")
                }
            }

            val httpCode = response.status.value
            println("[HIJRI-SYNC] HTTP $httpCode")

            if (!response.status.isSuccess()) {
                return RemoteFetchResult(
                    success = false,
                    data = emptyList(),
                    httpCode = httpCode,
                    error = "HTTP $httpCode"
                )
            }

            val raw = response.bodyAsText()

            if (raw.isBlank()) {
                return RemoteFetchResult(
                    success = true,
                    data = emptyList(),
                    httpCode = httpCode
                )
            }

            val root = try {
                parser.parseToJsonElement(raw)
            } catch (e: Exception) {
                return RemoteFetchResult(
                    success = false,
                    data = emptyList(),
                    httpCode = httpCode,
                    error = "JSON error: ${e.message}"
                )
            }

            val array = extractArray(root)

            if (array.isEmpty()) {
                return RemoteFetchResult(
                    success = true,
                    data = emptyList(),
                    httpCode = httpCode
                )
            }

            val parsed = array
                .mapNotNull(::parseItem)
                .filter { it.adjustment in -3..3 }
                .filter { validDate(it.startDate) }

            if (parsed.isEmpty()) {
                return RemoteFetchResult(
                    success = true,
                    data = emptyList(),
                    httpCode = httpCode,
                    error = "Tidak ada correction valid"
                )
            }

            val dedup = linkedMapOf<String, CascadeAdjustment>()
            parsed.forEach { dedup[it.startDate] = it }

            val result = dedup.values.sortedBy { it.startDate }

            println("[HIJRI-SYNC] Valid records=${result.size}")
            result.forEach {
                println("[HIJRI-SYNC] ${it.startDate} -> ${it.adjustment} ${it.desc}")
            }

            RemoteFetchResult(
                success = true,
                data = result,
                httpCode = httpCode
            )
        } catch (e: Exception) {
            println("[HIJRI-SYNC] Exception: ${e.message}")
            RemoteFetchResult(
                success = false,
                data = emptyList(),
                error = e.message
            )
        }
    }

    private fun extractArray(root: JsonElement): JsonArray = when (root) {
        is JsonArray -> root
        is JsonObject -> listOf("data", "adjustments", "items", "result")
            .asSequence()
            .mapNotNull { root[it] }
            .firstOrNull { it is JsonArray }
            ?.let { it as JsonArray }
            ?: JsonArray(emptyList())
        else -> JsonArray(emptyList())
    }

    private fun parseItem(element: JsonElement): CascadeAdjustment? {
        val obj = element as? JsonObject ?: return null

        val date = normalizeDate(
            firstString(obj, "startDate", "effectiveFrom", "date", "month")
                ?: return null
        ) ?: return null

        val offset = firstInt(
            obj, "adjustment", "offset", "value", "correction"
        ) ?: return null

        val desc = firstString(
            obj, "desc", "description", "note", "keterangan"
        ) ?: ""

        return CascadeAdjustment(
            startDate = date,
            adjustment = offset,
            desc = desc
        )
    }

    private fun firstString(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            obj[key]
                ?.jsonPrimitive
                ?.contentOrNull
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { return it }
        }
        return null
    }

    private fun firstInt(obj: JsonObject, vararg keys: String): Int? {
        for (key in keys) {
            val primitive = obj[key]?.jsonPrimitive ?: continue
            primitive.intOrNull?.let { return it }
            primitive.contentOrNull?.trim()?.toIntOrNull()?.let { return it }
        }
        return null
    }

    private fun normalizeDate(raw: String): String? {
        Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""")
            .matchEntire(raw.trim())
            ?.let {
                return "${it.groupValues[1]}-" +
                    it.groupValues[2].padStart(2, '0') + "-" +
                    it.groupValues[3].padStart(2, '0')
            }

        Regex("""^(\d{4})-(\d{1,2})$""")
            .matchEntire(raw.trim())
            ?.let {
                return "${it.groupValues[1]}-" +
                    it.groupValues[2].padStart(2, '0') + "-01"
            }

        return null
    }

    private fun validDate(date: String): Boolean {
        if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(date)) return false

        val parts = date.split("-")
        val month = parts[1].toIntOrNull() ?: return false
        val day = parts[2].toIntOrNull() ?: return false

        return month in 1..12 && day in 1..31
    }
}
