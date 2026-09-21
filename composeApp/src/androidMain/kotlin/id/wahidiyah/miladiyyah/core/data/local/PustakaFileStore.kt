package id.wahidiyah.miladiyyah.core.data.local

import android.content.Context
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem
import java.io.File
import java.security.MessageDigest

/**
 * Penyimpanan file Pustaka lokal Android.
 *
 * Tahap V1:
 * - Menentukan lokasi file lokal.
 * - Mengecek apakah file sudah tersimpan.
 * - Mengambil referensi File lokal.
 * - Menghapus file lokal.
 *
 * Belum melakukan download dan belum mengubah PustakaScreen.
 */
object PustakaFileStore {

    private const val DIRECTORY_NAME = "pustaka"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context {
        return appContext
            ?: error("PustakaFileStore belum diinisialisasi.")
    }

    private fun storageDirectory(): File {
        return File(
            requireContext().filesDir,
            DIRECTORY_NAME
        ).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Nama file stabil berdasarkan URL/link Pustaka.
     * URL yang sama -> file lokal yang sama.
     */
    private fun fileName(item: PustakaItem): String {
        val hash = sha256(item.link.trim())

        val extension = extractExtension(item.link)
        return if (extension.isNotBlank()) {
            "$hash.$extension"
        } else {
            hash
        }
    }

    fun getLocalFile(item: PustakaItem): File {
        return File(
            storageDirectory(),
            fileName(item)
        )
    }

    fun isDownloaded(item: PustakaItem): Boolean {
        val file = getLocalFile(item)
        return file.exists() && file.isFile && file.length() > 0L
    }

    fun delete(item: PustakaItem): Boolean {
        val file = getLocalFile(item)

        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    private fun extractExtension(link: String): String {
        val clean = link
            .substringBefore('?')
            .substringBefore('#')
            .trim()

        val lastPart = clean.substringAfterLast('/')

        if (!lastPart.contains('.')) return ""

        val extension = lastPart
            .substringAfterLast('.')
            .lowercase()

        return extension
            .takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
            .orEmpty()
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }
}
