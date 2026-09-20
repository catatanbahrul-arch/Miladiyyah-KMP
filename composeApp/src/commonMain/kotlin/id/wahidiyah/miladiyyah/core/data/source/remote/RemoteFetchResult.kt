package id.wahidiyah.miladiyyah.core.data.source.remote

data class RemoteFetchResult<T>(
    val success: Boolean,
    val data: T,
    val httpCode: Int? = null,
    val error: String? = null
)
