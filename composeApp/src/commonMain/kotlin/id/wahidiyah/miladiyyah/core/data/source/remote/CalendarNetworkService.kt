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
data class GasAdjustment(val month: String, val adjustment: Int, val desc: String)

@Serializable
data class GasResponse(val status: String, val data: List<GasAdjustment>)

class CalendarNetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun fetchAdjustments(url: String): Map<String, Int> {
        return try {
            // ANTI-CACHE: Menambahkan waktu saat ini ke URL agar Google/Ktor mengira ini link baru 
            // dan dipaksa membaca ulang langsung dari Google Sheets.
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val noCacheUrl = if (url.contains("?")) "$url&t=$timeStamp" else "$url?t=$timeStamp"
            
            val response: GasResponse = client.get(noCacheUrl).body()
            if (response.status == "success") {
                response.data.associate { it.month to it.adjustment }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            println("Gagal sinkronisasi kalender: ${e.message}")
            emptyMap()
        }
    }
}
