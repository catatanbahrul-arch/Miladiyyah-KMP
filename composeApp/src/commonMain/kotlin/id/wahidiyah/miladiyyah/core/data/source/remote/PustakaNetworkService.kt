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
data class PustakaItem(
    val title: String,
    val content: String,
    val link: String
)

object PustakaRepository {

    private val client = HttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private const val BASE_URL =
        "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=pustaka"

    suspend fun fetchPustaka(): List<PustakaItem> {
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
                println("[PUSTAKA] HTTP ${response.status}")
                return emptyList()
            }

            val raw = response.bodyAsText()
            println("[PUSTAKA] ${raw.take(700)}")

            val root = json.parseToJsonElement(raw)
            val array = extractArray(root)

            val result = array.mapNotNull { element ->
                val obj = element as? JsonObject ?: return@mapNotNull null

                val title = first(
                    obj, "title", "judul", "nama", "name"
                ).orEmpty()

                val content = first(
                    obj, "content", "isi", "description", "keterangan"
                ).orEmpty()

                val link = first(
                    obj, "link", "url", "file", "fileUrl", "download"
                ).orEmpty()

                if (title.isBlank() && content.isBlank() && link.isBlank()) {
                    null
                } else {
                    PustakaItem(
                        title = title.trim(),
                        content = content.trim(),
                        link = link.trim()
                    )
                }
            }

            println("[PUSTAKA] parsed=${result.size}")
            result

        } catch (e: Exception) {
            println("[PUSTAKA] gagal: ${e.message}")
            emptyList()
        }
    }

    private fun extractArray(root: JsonElement): JsonArray =
        when (root) {
            is JsonArray -> root
            is JsonObject -> listOf(
                "data", "items", "pustaka", "result"
            ).asSequence()
                .mapNotNull { root[it] }
                .firstOrNull { it is JsonArray }
                ?.jsonArray
                ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
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
