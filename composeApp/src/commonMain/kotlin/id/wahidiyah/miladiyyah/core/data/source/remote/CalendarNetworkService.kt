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
data class CascadeAdjustment(val startDate: String, val adjustment: Int, val desc: String)

@Serializable
data class GasCascadeResponse(val status: String, val data: List<CascadeAdjustment>)

class CalendarNetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun fetchCascadeAdjustments(url: String): List<CascadeAdjustment> {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val noCacheUrl = if (url.contains("?")) "$url&t=$timeStamp" else "$url?t=$timeStamp"
            
            val response: GasCascadeResponse = client.get(noCacheUrl).body()
            if (response.status == "success") {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Gagal sinkronisasi kalender berantai: ${e.message}")
            emptyList()
        }
    }
}
