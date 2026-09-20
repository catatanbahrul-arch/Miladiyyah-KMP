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
        "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=pengumuman"

    suspend fun fetchPengumuman(): PengumumanData? {
        return try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val response = client.get("$BASE_URL&t=$timestamp") {
                headers {
                    append("Cache-Control", "no-cache, no-store, max-age=0")
                    append("Pragma", "no-cache")
                    append("Accept", "application/json, text/plain, */*")
                }
            }

            if (!response.status.isSuccess()) {
                println("[PENGUMUMAN] HTTP ${response.status}")
                return null
            }

            val raw = response.bodyAsText()
            println("[PENGUMUMAN] ${raw.take(700)}")

            val root = json.parseToJsonElement(raw)
            val obj = extractObject(root) ?: return null

            val title = first(obj, "title", "judul", "nama").orEmpty()
            val content = first(
                obj, "content", "isi", "description", "keterangan", "pesan", "message"
            ).orEmpty()
            val link = first(obj, "link", "url", "file", "dokumen").orEmpty()

            if (title.isBlank() && content.isBlank()) return null

            PengumumanData(
                title = title,
                content = content,
                link = link
            )

        } catch (e: Exception) {
            println("[PENGUMUMAN] gagal: ${e.message}")
            null
        }
    }

    private fun extractObject(root: JsonElement): JsonObject? {
        if (root is JsonObject) {
            val data = root["data"]
            if (data is JsonObject) return data.jsonObject

            val result = root["result"]
            if (result is JsonObject) return result.jsonObject

            return root.jsonObject
        }

        if (root is JsonArray) {
            val first = root.firstOrNull()
            if (first is JsonObject) return first.jsonObject
        }

        return null
    }

    private fun first(
        obj: JsonObject,
        vararg keys: String
    ): String? {
        keys.forEach { key ->
            val value = obj[key]?.jsonPrimitive?.contentOrNull
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
