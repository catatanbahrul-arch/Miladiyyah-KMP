package id.wahidiyah.miladiyyah.core.data.local

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

actual object PustakaDownloadManager {

    private var appContext: Context? = null

    /**
     * Dipanggil sekali dari MainActivity.
     */
    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        PustakaFileStore.initialize(applicationContext)
    }

    private fun requireInitialized() {
        check(appContext != null) {
            "PustakaDownloadManager belum diinisialisasi."
        }
    }

    actual fun isDownloaded(item: PustakaItem): Boolean {
        requireInitialized()
        return PustakaFileStore.isDownloaded(item)
    }

    actual fun open(item: PustakaItem): Boolean {
        requireInitialized()

        val context = requireNotNull(appContext)
        val file = PustakaFileStore.getLocalFile(item)

        if (!file.exists() || file.length() <= 0L) {
            return false
        }

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )

            val mimeType =
                MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(
                        file.extension.lowercase()
                    )
                    ?: "application/octet-stream"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    actual fun delete(item: PustakaItem): Boolean {
        requireInitialized()
        return PustakaFileStore.delete(item)
    }

    actual suspend fun download(
        item: PustakaItem,
        onProgress: suspend (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {

        requireInitialized()

        val rawUrl = item.link.trim()

        if (!rawUrl.startsWith("http://") &&
            !rawUrl.startsWith("https://")
        ) {
            throw IOException("Link Pustaka bukan URL HTTP/HTTPS.")
        }

        val downloadUrl = normalizeDownloadUrl(rawUrl)

        val connection =
            (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                useCaches = false
                setRequestProperty(
                    "User-Agent",
                    "WAHIDIYAH-Android-Pustaka/1.0"
                )
            }

        try {
            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw IOException(
                    "Download gagal. HTTP $responseCode"
                )
            }

            val contentType =
                connection.contentType
                    ?.substringBefore(";")
                    ?.trim()
                    ?.lowercase()
                    .orEmpty()

            /*
             * Jika server mengembalikan HTML, kemungkinan yang diterima
             * adalah halaman web/Google Drive, bukan file fisik.
             */
            if (
                contentType == "text/html" ||
                contentType == "application/xhtml+xml"
            ) {
                throw IOException(
                    "Server mengembalikan halaman HTML, bukan file. " +
                    "Pastikan link Pustaka mengarah ke file yang dapat diunduh."
                )
            }

            val extensionFromMime =
                extensionFromContentType(contentType)

            val target =
                PustakaFileStore.getLocalFile(
                    item = item,
                    preferredExtension = extensionFromMime
                )

            target.parentFile?.mkdirs()

            val temp = File(
                target.parentFile,
                target.name + ".part"
            )

            if (temp.exists()) {
                temp.delete()
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L
            var lastProgress = -1

            onProgress(0)

            connection.inputStream.use { input ->
                temp.outputStream().use { output ->

                    val buffer = ByteArray(64 * 1024)

                    while (true) {
                        val read = input.read(buffer)

                        if (read <= 0) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        if (totalBytes > 0L) {
                            val progress =
                                (
                                    downloadedBytes * 100L /
                                        totalBytes
                                )
                                    .toInt()
                                    .coerceIn(0, 99)

                            if (progress != lastProgress) {
                                lastProgress = progress
                                onProgress(progress)
                            }
                        }
                    }

                    output.flush()
                }
            }

            if (!temp.exists() || temp.length() <= 0L) {
                temp.delete()
                throw IOException("File hasil download kosong.")
            }

            if (target.exists()) {
                target.delete()
            }

            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }

            onProgress(100)

            true
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Mendukung beberapa bentuk link Google Drive umum.
     * Link lain tetap digunakan apa adanya.
     */
    private fun extensionFromContentType(
        contentType: String
    ): String {
        return when (
            contentType
                .substringBefore(";")
                .trim()
                .lowercase()
        ) {
            "application/pdf" -> "pdf"

            "image/jpeg",
            "image/jpg" -> "jpg"

            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"

            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            "video/quicktime" -> "mov"

            "audio/mpeg" -> "mp3"
            "audio/mp4" -> "m4a"
            "audio/wav",
            "audio/x-wav" -> "wav"
            "audio/ogg" -> "ogg"

            else -> ""
        }
    }

    private fun normalizeDownloadUrl(rawUrl: String): String {
        return try {
            val uri = Uri.parse(rawUrl)
            val host = uri.host
                ?.lowercase()
                .orEmpty()

            if (!host.contains("drive.google.com")) {
                return rawUrl
            }

            val path = uri.path.orEmpty()

            val fileIdFromPath =
                Regex("/file/d/([^/]+)")
                    .find(path)
                    ?.groupValues
                    ?.getOrNull(1)

            val fileId =
                fileIdFromPath
                    ?: uri.getQueryParameter("id")

            if (!fileId.isNullOrBlank()) {
                "https://drive.google.com/uc?export=download&id=$fileId"
            } else {
                rawUrl
            }
        } catch (_: Exception) {
            rawUrl
        }
    }
}
