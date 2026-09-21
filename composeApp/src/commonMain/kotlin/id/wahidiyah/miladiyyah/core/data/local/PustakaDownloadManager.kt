package id.wahidiyah.miladiyyah.core.data.local

import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem

expect object PustakaDownloadManager {

    fun isDownloaded(item: PustakaItem): Boolean

    suspend fun download(
        item: PustakaItem,
        onProgress: suspend (Int) -> Unit
    ): Boolean

    fun open(item: PustakaItem): Boolean

    fun delete(item: PustakaItem): Boolean
}
