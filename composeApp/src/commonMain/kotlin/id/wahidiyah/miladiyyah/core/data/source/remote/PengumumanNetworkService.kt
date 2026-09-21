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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class PengumumanData(
    val title: String,
    val content: String,
    val link: String
)

object PengumumanRepository {
    private val client = HttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private const val BASE_URL =
        GasEndpointConfig.PENGUMUMAN_PENTING

    suspend fun fetchPengumuman(): RemoteFetchResult<PengumumanData?> {
        return try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val endpoint =
                if (BASE_URL.contains("?")) {
                    "$BASE_URL&t=$timestamp"
                } else {
                    "$BASE_URL?t=$timestamp"
                }

            val response = client.get(endpoint) {
                headers {
                    append("Cache-Control", "no-cache, no-store, max-age=0")
                    append("Pragma", "no-cache")
                    append("Accept", "application/json, text/plain, */*")
                }
            }

            val httpCode = response.status.value
            if (!response.status.isSuccess()) {
                return RemoteFetchResult(false, null, httpCode, "HTTP $httpCode")
            }

            val raw = response.bodyAsText()
            if (raw.isBlank()) {
                return RemoteFetchResult(true, null, httpCode)
            }

            val root = try {
                json.parseToJsonElement(raw)
            } catch (e: Exception) {
                return RemoteFetchResult(false, null, httpCode, "JSON error: ${e.message}")
            }

            val obj = extractObject(root)
                ?: return RemoteFetchResult(true, null, httpCode)

            val title = first(obj, "title", "judul", "nama").orEmpty()
            val content = first(
                obj,
                "content",
                "isi",
                "description",
                "keterangan",
                "pesan",
                "message"
            ).orEmpty()
            val link = first(obj, "link", "url", "file", "dokumen").orEmpty()

            val data = if (title.isBlank() && content.isBlank() && link.isBlank()) {
                null
            } else {
                PengumumanData(title, content, link)
            }

            RemoteFetchResult(true, data, httpCode)
        } catch (e: Exception) {
            RemoteFetchResult(false, null, error = e.message)
        }
    }

    private fun extractObject(root: JsonElement): JsonObject? {
        if (root is JsonObject) {
            val data = root["data"]
            if (data is JsonObject) return data.jsonObject

            val result = root["result"]
            if (result is JsonObject) return result.jsonObject

            return null
        }

        if (root is JsonArray) {
            val first = root.firstOrNull()
            if (first is JsonObject) return first.jsonObject
        }

        return null
    }

    private fun first(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val value = obj[key]?.jsonPrimitive?.contentOrNull
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
