package id.wahidiyah.miladiyyah.core.data.source.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PengumumanData(val title: String, val content: String, val link: String)

@Serializable
data class PengumumanResponse(val status: String, val data: PengumumanData?)

object PengumumanRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=pengumuman"

    suspend fun fetchPengumuman(): PengumumanData? {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val url = "$BASE_URL&t=$timeStamp"
            val response: PengumumanResponse = client.get(url).body()
            if (response.status == "success") {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            println("Gagal memuat pengumuman: ${e.message}")
            null
        }
    }
}
